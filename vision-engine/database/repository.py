# C:\Users\john\Desktop\eduvision\vision-engine\database\repository.py

import logging
from database.mysql_connector import get_connection

# Add logger
logger = logging.getLogger(__name__)

def ensure_session(session_id: str):
    """Ensure session exists in database"""
    try:
        con = get_connection()
        cur = con.cursor()
        cur.execute("INSERT IGNORE INTO sessions(session_id) VALUES (%s)", (session_id,))
        con.commit()
        cur.close()
        con.close()
    except Exception as e:
        logger.error(f"Ensure session error: {e}")

def insert_detection(session_id: str, detection):
    """Insert a face detection record"""
    try:
        # Handle both dict and list formats
        if isinstance(detection, list):
            for det in detection:
                _insert_single_detection(session_id, det)
        else:
            _insert_single_detection(session_id, detection)
    except Exception as e:
        logger.error(f"Insert detection error: {e}")

def _insert_single_detection(session_id: str, detection: dict):
    """Insert a single detection record"""
    try:
        conn = get_connection()
        cursor = conn.cursor()
        
        # Handle concentration value (convert from string to float if needed)
        concentration = detection.get("concentration", 0.5)
        if isinstance(concentration, str):
            concentration_map = {
                "low": 0.3,
                "medium": 0.6,
                "high": 0.9,
                "very_low": 0.1,
                "very_high": 0.95
            }
            concentration = concentration_map.get(concentration.lower(), 0.5)
        elif isinstance(concentration, dict):
            concentration = concentration.get("level", 0.5)
            if isinstance(concentration, str):
                conc_map = {"low": 0.3, "medium": 0.6, "high": 0.9}
                concentration = conc_map.get(concentration.lower(), 0.5)
        
        # Handle confidence score
        confidence = detection.get("emotion_confidence", detection.get("confidence", 0.5))
        if isinstance(confidence, str):
            try:
                confidence = float(confidence)
            except:
                confidence = 0.5
        
        cursor.execute("""
            INSERT INTO face_detections 
            (id, session_id, student_id, emotion, concentration, confidence, bounding_box, detected_at)
            VALUES (UUID(), %s, %s, %s, %s, %s, %s, NOW())
        """, (
            session_id,
            detection.get("student_id"),
            detection.get("dominant_emotion", detection.get("emotion", "neutral")),
            concentration,
            confidence,
            str(detection.get("bounding_box", {}))
        ))
        
        conn.commit()
        cursor.close()
        conn.close()
    except Exception as e:
        logger.debug(f"Single detection insert error: {e}")