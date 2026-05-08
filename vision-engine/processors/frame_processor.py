# C:\Users\john\Desktop\eduvision\vision-engine\processors\frame_processor.py
import logging
from datetime import datetime
from typing import Dict, Any
from processors.face_analyzer import FaceAnalyzer
from processors.aggregator import Aggregator
from processors.presence_tracker import PresenceTracker

logger = logging.getLogger(__name__)

ATTENDANCE_URL = "http://localhost:8080/api/v1/attendance"

# Minimum cosine similarity required to count a detection as valid presence.
# Must be higher than the recogniser's own threshold (0.40) so that
# low-confidence / false-positive matches on covered-camera frames
# don't reset last_seen_at and prevent the leave timer from firing.
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