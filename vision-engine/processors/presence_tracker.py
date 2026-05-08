# C:\Users\john\Desktop\eduvision\vision-engine\processors\presence_tracker.py

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
        self.student_states = {}  # key: f"{session_id}:{student_id}" -> StudentState
        
    def update_presence(self, session_id: str, student_id: str, student_name: str, current_time: datetime):
        """Update presence when student is detected by camera"""
        
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
        
        # Check if student was previously marked as away and now returned
        if state.is_away and time_since_last_seen < self.GRACE_PERIOD_SECONDS:
            state.is_away = False
            state.return_count += 1
            logger.info(f"🔄 RETURNED: {student_name} (was away for {int(time_since_last_seen)}s)")
            return "returned"
        elif state.is_away:
            return "still_away"
        else:
            return "present"
    
    def check_away_students(self, session_id: str, current_time: datetime):
        """Check for students who have been unseen longer than grace period"""
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
    """Stores state for a single student"""
    
    def __init__(self, session_id: str, student_id: str, student_name: str, joined_at: datetime):
        self.session_id = session_id
        self.student_id = student_id
        self.student_name = student_name
        self.joined_at = joined_at
        self.last_seen_at = joined_at
        self.left_at = None
        self.is_away = False
        self.return_count = 0