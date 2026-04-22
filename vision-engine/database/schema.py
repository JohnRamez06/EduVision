from database.mysql_connector import get_connection


def ensure_schema():
    """
    Creates tables if they do not exist.
    Minimal schema: sessions, detections (per face per frame).
    """
    con = get_connection()
    cur = con.cursor()

    cur.execute("""
    CREATE TABLE IF NOT EXISTS sessions (
      id BIGINT PRIMARY KEY AUTO_INCREMENT,
      session_id VARCHAR(64) NOT NULL,
      started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      UNIQUE KEY uq_session (session_id)
    )
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS detections (
      id BIGINT PRIMARY KEY AUTO_INCREMENT,
      session_id VARCHAR(64) NOT NULL,
      captured_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

      face_index INT NOT NULL,
      in_class BOOLEAN NOT NULL,

      box_x INT NOT NULL,
      box_y INT NOT NULL,
      box_w INT NOT NULL,
      box_h INT NOT NULL,

      dominant_emotion VARCHAR(32) NOT NULL,
      emotion_confidence DOUBLE NOT NULL,

      concentration_level VARCHAR(16) NOT NULL,
      concentration_score DOUBLE NOT NULL,

      drowsiness_score DOUBLE NOT NULL
    )
    """)

    cur.close()
    con.close()