# =============================================================================
# frame_processor.py — Per-frame face analysis orchestrator
#
# This module is the central coordinator for everything that happens each time
# a video frame arrives from the lecturer's browser.  It chains together three
# sub-processors:
#
#   FaceAnalyzer    — detects faces in the BGR frame and extracts emotion data
#   PresenceTracker — maintains per-student join/leave/return state
#   Aggregator      — computes class-level statistics (emotion counts, engagement)
#
# DATA FLOW PER FRAME:
#   raw BGR frame
#     → FaceAnalyzer.analyze()       → list of detected people (with emotion)
#     → PresenceTracker.update_presence() per person → presence_event label
#     → PresenceTracker.check_away_students()         → list of students who left
#     → POST leave events to Spring Boot (via self.spring_client)
#     → return aggregated result dict to main.py
#
# The result dict is then used by main.py's send_to_spring_boot() to flush
# a class snapshot + per-student snapshots to Spring Boot every 10 seconds.
# =============================================================================

import logging
from datetime import datetime
from typing import Dict, Any
from processors.face_analyzer import FaceAnalyzer
from processors.aggregator import Aggregator
from processors.presence_tracker import PresenceTracker

logger = logging.getLogger(__name__)

ATTENDANCE_URL = "http://localhost:8080/api/v1/attendance"

# Minimum cosine similarity required to count a detection as valid presence.
#
# WHY HIGHER THAN THE RECOGNIZER THRESHOLD (0.40)?
# The face recognizer uses 0.40 as its match threshold — below that score a
# face is considered unknown.  However, a match with a score between 0.40 and
# 0.55 may be a low-confidence or false-positive match (e.g., when the camera
# is covered, dark, or pointed at a blank wall and the model still returns
# someone's name with a marginal score).
#
# If we allowed those low-confidence matches to reset last_seen_at in the
# PresenceTracker, the "time since last seen" counter would never grow large
# enough to exceed the grace period, so students would never be marked as
# having left.  By raising the bar to 0.55 here, we ensure that only
# genuine, confident detections keep a student's presence clock fresh.
PRESENCE_SIMILARITY_THRESHOLD = 0.55

class FrameProcessor:
    def __init__(self):
        self.face_analyzer = FaceAnalyzer()
        self.aggregator = Aggregator()
        self.presence_tracker = PresenceTracker()
        self.current_session_id = None
        self.spring_client = None          # will be set from main.py

    # ... (keep _convert_concentration & other helpers as they are) ...

    def process(self, frame_bgr) -> Dict[str, Any]:
        """
        Orchestrates full face analysis for a single video frame.

        FLOW:
          1. Detect faces — FaceAnalyzer.analyze() returns a list of people
             dicts, each containing student_id, student_name, emotion,
             concentration, similarity, and confidence fields.

          2. Recognize and update presence — For each detected person whose
             cosine similarity >= PRESENCE_SIMILARITY_THRESHOLD, call
             PresenceTracker.update_presence().  The tracker returns one of:
               "joined"   — first time we've seen this student this session
               "returned" — student was marked away and is now back
               "present"  — routine detection, no state change

             Only high-confidence matches are counted as real presence so
             that camera obstructions don't prevent the leave timer from firing.

          3. Detect leaves — PresenceTracker.check_away_students() scans all
             tracked students and returns those unseen beyond the grace period.
             For each leaving student, two HTTP calls are made to Spring Boot:
               a. POST /attendance/record-with-presence (presenceEvent="left")
                  to set left_at in session_attendance.
               b. POST /attendance/exit to insert a row in session_exit_logs.

          4. Return aggregated result dict — consumed by main.py to build
             the class-snapshot + student-snapshot payloads for Spring Boot.

        Returns a dict with keys: people, student_count, emotion_counts,
        engagement_score, avg_concentration, dominant_emotion.
        """
        try:
            analysis = self.face_analyzer.analyze(frame_bgr)
            if isinstance(analysis, list):
                people = analysis
            elif isinstance(analysis, dict):
                people = analysis.get("people", analysis.get("faces", []))
            else:
                people = []

            current_time = datetime.now()

            # Update presence for detected faces
            for person in people:
                student_id = person.get("student_id")
                student_name = person.get("student_name", "Unknown")
                similarity = person.get("similarity", 0.0)

                # Only count this detection as real presence if the recogniser is
                # confident enough.  Low-similarity matches come from false-positive
                # Haar detections on covered/dark frames and must NOT reset
                # last_seen_at, otherwise the leave timer never fires.
                if student_id and self.current_session_id and similarity >= PRESENCE_SIMILARITY_THRESHOLD:
                    event = self.presence_tracker.update_presence(
                        self.current_session_id, student_id, student_name, current_time
                    )
                    person["presence_event"] = event
                    logger.info(f"👤 {student_name} (sim={similarity:.2f}): {event}")
                    # Close the open exit log when student returns after a leave
                    if event == "returned" and self.spring_client:
                        try:
                            ret_resp = self.spring_client.post(
                                f"{ATTENDANCE_URL}/return",
                                json={"sessionId": self.current_session_id, "studentId": student_id}
                            )
                            if ret_resp.status_code in [200, 201]:
                                logger.info(f"✅ Return logged for {student_name}")
                            else:
                                logger.warning(f"Failed to log return: {ret_resp.status_code}")
                        except Exception as e:
                            logger.error(f"Error logging return: {e}")
                elif student_id and similarity < PRESENCE_SIMILARITY_THRESHOLD:
                    logger.debug(f"⚠️ {student_name} sim={similarity:.2f} below threshold — not counting as present")

            # ---- LEAVE DETECTION & SENDING ----
            if self.current_session_id:
                left_students = self.presence_tracker.check_away_students(
                    self.current_session_id, current_time
                )
                for left in left_students:
                    logger.info(f"📤 LEAVE DETECTED: {left['student_name']} at {left['left_at']}")
                    if self.spring_client:
                        try:
                            # 1. Update left_at in session_attendance
                            leave_payload = {
                                "sessionId": self.current_session_id,
                                "studentId": left["student_id"],
                                "presenceEvent": "left",
                                "leftAt": left["left_at"].isoformat()
                            }
                            resp = self.spring_client.post(
                                f"{ATTENDANCE_URL}/record-with-presence",
                                json=leave_payload
                            )
                            if resp.status_code in [200, 201]:
                                logger.info(f"✅ left_at updated for {left['student_name']}")
                            else:
                                logger.warning(f"Failed to update left_at: {resp.status_code}")

                            # 2. Insert a row into session_exit_logs
                            exit_payload = {
                                "sessionId": self.current_session_id,
                                "studentId": left["student_id"],
                                "exitType": "automatic"
                            }
                            exit_resp = self.spring_client.post(
                                f"{ATTENDANCE_URL}/exit",
                                json=exit_payload
                            )
                            if exit_resp.status_code in [200, 201]:
                                logger.info(f"✅ Exit log created for {left['student_name']}")
                            else:
                                logger.warning(f"Failed to create exit log: {exit_resp.status_code}")
                        except Exception as e:
                            logger.error(f"Error sending leave event: {e}")

            return {
                "people": people,
                "student_count": len(people),
                "emotion_counts": self._calc_emotion_counts(people),
                "engagement_score": self._calc_engagement(people),
                "avg_concentration": self._calc_avg_concentration(people),
                "dominant_emotion": self._get_dominant_emotion(people)
            }

        except Exception as e:
            logger.error(f"Frame processing error: {e}")
            return {
                "people": [],
                "student_count": 0,
                "emotion_counts": {},
                "engagement_score": 0.5,
                "avg_concentration": 0.5,
                "dominant_emotion": "neutral"
            }

    # --- helper methods unchanged ---
    def _calc_emotion_counts(self, people): ...
    def _calc_avg_concentration(self, people): ...
    def _calc_engagement(self, people): ...
    def _get_dominant_emotion(self, people): ...
    def set_session(self, session_id: str): self.current_session_id = session_id
    def clear_session_presence(self): ...
