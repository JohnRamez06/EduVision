# =============================================================================
# presence_tracker.py — Student join/leave/return state machine
#
# This module tracks the real-time presence of each student during a session.
# It maintains a per-student state machine with three effective states:
#
#   JOINED   — first time the student was detected in this session
#   PRESENT  — student is continuously detected (no state change)
#   AWAY     — student has not been detected for longer than GRACE_PERIOD_SECONDS
#
# When a student transitions from AWAY back to detected, they are marked as
# RETURNED and their is_away flag is cleared.
#
# WHY A GRACE PERIOD?
#   Camera-based detection is imperfect.  A student may briefly look away, lean
#   forward, or be occluded by another student for a few seconds.  Without a
#   grace period every brief occlusion would generate a false "left" event,
#   cluttering the exit log and the lecturer's dashboard.  The grace period
#   (currently 10 s for testing, intended to be 60 s in production) absorbs
#   these transient gaps before committing a leave event to Spring Boot.
#
# HOW RESULTS REACH SPRING BOOT:
#   FrameProcessor.process() calls check_away_students() after processing each
#   frame.  Any returned left-students are immediately POSTed to Spring Boot
#   via /api/v1/attendance/record-with-presence and /api/v1/attendance/exit.
# =============================================================================

from datetime import datetime
import logging

logger = logging.getLogger(__name__)

class PresenceTracker:
    """
    Tracks student presence during a session.
    Grace period prevents false leaves from camera blind spots.
    """

    GRACE_PERIOD_SECONDS = 10  # For testing – change back to 60 later

    def __init__(self):
        # Keyed by "{session_id}:{student_id}" → StudentState
        self.student_states = {}

    def update_presence(self, session_id: str, student_id: str, student_name: str, current_time: datetime):
        """
        Updates a student's presence state when they are detected in a frame.

        STATE TRANSITIONS:
          - First detection ever in this session:
              Create a new StudentState with joined_at = current_time.
              Return "joined" so main.py records the attendance event.

          - Detected while is_away=True (student just returned):
              Clear the is_away flag, increment return_count.
              Return "returned" so main.py posts a return event and closes
              the open exit_log row in Spring Boot.

          - Detected while already present (is_away=False):
              Simply refresh last_seen_at to keep the leave timer from firing.
              Return "present" — no attendance action needed.

        In all cases, last_seen_at is updated to current_time so that
        check_away_students() always works from the most recent sighting.
        """

        key = f"{session_id}:{student_id}"

        if key not in self.student_states:
            # First detection - student joined
            self.student_states[key] = StudentState(
                session_id=session_id,
                student_id=student_id,
                student_name=student_name,
                joined_at=current_time
            )
            logger.info(f"✅ JOINED: {student_name} at {current_time.strftime('%H:%M:%S')}")
            return "joined"

        state = self.student_states[key]
        last_seen = state.last_seen_at
        time_since_last_seen = (current_time - last_seen).total_seconds()

        # Update last seen time
        state.last_seen_at = current_time

        # If student was marked away and is now detected again, they have returned —
        # regardless of how long they were gone (grace period only governs marking as left)
        if state.is_away:
            state.is_away = False
            state.return_count += 1
            logger.info(f"🔄 RETURNED: {student_name} (was away for {int(time_since_last_seen)}s)")
            return "returned"
        else:
            return "present"

    def check_away_students(self, session_id: str, current_time: datetime):
        """
        Scans all tracked students and identifies those who have been unseen
        for longer than GRACE_PERIOD_SECONDS.

        HOW THE GRACE PERIOD PREVENTS FALSE LEAVES:
          After every frame, this method is called with the current timestamp.
          For each student, it computes:
              time_since_last_seen = current_time - state.last_seen_at
          If that gap exceeds GRACE_PERIOD_SECONDS AND the student is not
          already marked as away, the student is considered to have left.
          Their is_away flag is set to True and left_at is recorded.

          Only after the grace period expires do we commit a leave event to
          Spring Boot, preventing a 2-second camera glitch from being logged
          as a departure in the attendance record.

        Returns a list of dicts for students newly marked as left:
          { student_id, student_name, left_at (datetime), type: "left" }
        """
        results = []

        for key, state in self.student_states.items():
            if not key.startswith(f"{session_id}:"):
                continue

            if state.is_away:
                continue

            time_since_last_seen = (current_time - state.last_seen_at).total_seconds()

            # Log every time we check – now INFO level so you see it
            logger.info(f"⏱️ {state.student_name}: last seen {time_since_last_seen:.1f}s ago (grace={self.GRACE_PERIOD_SECONDS}s)")

            if time_since_last_seen > self.GRACE_PERIOD_SECONDS:
                # Student has been unseen too long – mark as left
                state.is_away = True
                state.left_at = state.last_seen_at
                logger.info(f"🚪 LEFT: {state.student_name} at {state.last_seen_at.strftime('%H:%M:%S')}")
                results.append({
                    "student_id": state.student_id,
                    "student_name": state.student_name,
                    "left_at": state.last_seen_at,
                    "type": "left"
                })

        return results

    def get_session_summary(self, session_id: str):
        """Get summary for all students in session"""
        summary = []

        for key, state in self.student_states.items():
            if not key.startswith(f"{session_id}:"):
                continue

            summary.append({
                "student_id": state.student_id,
                "student_name": state.student_name,
                "joined_at": state.joined_at,
                "left_at": state.left_at,
                "was_present": state.joined_at is not None
            })

        return summary

    def clear_session(self, session_id: str):
        """Clear all states for a session"""
        keys_to_delete = [k for k in self.student_states.keys() if k.startswith(f"{session_id}:")]
        for key in keys_to_delete:
            del self.student_states[key]


class StudentState:
    """Stores state for a single student within one session."""

    def __init__(self, session_id: str, student_id: str, student_name: str, joined_at: datetime):
        self.session_id = session_id
        self.student_id = student_id
        self.student_name = student_name
        self.joined_at = joined_at
        self.last_seen_at = joined_at   # refreshed on every confident detection
        self.left_at = None             # set when grace period expires
        self.is_away = False            # True while student is beyond the grace period
        self.return_count = 0           # how many times the student has returned after leaving
