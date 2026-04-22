from database.mysql_connector import get_connection


def ensure_session(session_id: str):
    con = get_connection()
    cur = con.cursor()
    cur.execute("INSERT IGNORE INTO sessions(session_id) VALUES (%s)", (session_id,))
    cur.close()
    con.close()


def insert_detection(session_id: str, det: dict):
    """
    det comes from FaceAnalyzer output per person.
    """
    b = det["bounding_box"]
    con = get_connection()
    cur = con.cursor()
    cur.execute(
        """
        INSERT INTO detections(
          session_id, face_index, in_class,
          box_x, box_y, box_w, box_h,
          dominant_emotion, emotion_confidence,
          concentration_level, concentration_score,
          drowsiness_score
        )
        VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)
        """,
        (
            session_id,
            int(det["face_index"]),
            bool(det["in_class"]),
            int(b["x"]), int(b["y"]), int(b["w"]), int(b["h"]),
            str(det["dominant_emotion"]),
            float(det["emotion_confidence"]),
            str(det["concentration"]["level"]),
            float(det["concentration"]["score"]),
            float(det["drowsiness_score"]),
        ),
    )
    cur.close()
    con.close()