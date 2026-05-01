# face_recognizer.py
import numpy as np
import mysql.connector
from typing import Optional
import logging

logger = logging.getLogger(__name__)

DB_CONFIG = {
    "host": "localhost",
    "user": "root",
    "password": "",
    "database": "eduvision"
}

SIMILARITY_THRESHOLD = 0.6


def load_embeddings() -> list[dict]:
    """
    Load all students with non-null face encodings from MySQL.
    Returns list of {student_id: str, embedding_vector: np.ndarray}
    """
    embeddings = []
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor()
        cursor.execute(
            "SELECT student_id, face_encoding FROM students WHERE face_encoding IS NOT NULL"
        )
        rows = cursor.fetchall()
        for student_id, blob in rows:
            try:
                vector = np.frombuffer(blob, dtype=np.float32)
                embeddings.append({"student_id": student_id, "embedding_vector": vector})
            except Exception as e:
                logger.warning(f"Failed to decode embedding for student {student_id}: {e}")
        cursor.close()
        conn.close()
        logger.info(f"Loaded {len(embeddings)} face embeddings from database.")
    except mysql.connector.Error as e:
        logger.error(f"Database error loading embeddings: {e}")
    return embeddings


def _cosine_similarity(vec_a: np.ndarray, vec_b: np.ndarray) -> float:
    norm_a = np.linalg.norm(vec_a)
    norm_b = np.linalg.norm(vec_b)
    if norm_a == 0 or norm_b == 0:
        return 0.0
    return float(np.dot(vec_a, vec_b) / (norm_a * norm_b))


class FaceRecognizer:
    """Singleton face recognizer that holds loaded embeddings in memory."""

    _instance = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._embeddings = []
            cls._instance._loaded = False
        return cls._instance

    def load(self):
        self._embeddings = load_embeddings()
        self._loaded = True

    def reload(self):
        """Refresh embeddings from DB (call when new students enroll)."""
        self.load()

    def identify_face(self, face_embedding: np.ndarray) -> Optional[str]:
        """
        Compare face_embedding against all stored embeddings.
        Returns student_id string if best match similarity > 0.6, else None.
        """
        if not self._loaded:
            self.load()

        best_id = None
        best_score = -1.0

        for entry in self._embeddings:
            score = _cosine_similarity(face_embedding, entry["embedding_vector"])
            if score > best_score:
                best_score = score
                best_id = entry["student_id"]

        if best_score >= SIMILARITY_THRESHOLD:
            logger.debug(f"Identified student {best_id} with similarity {best_score:.3f}")
            return best_id

        logger.debug(f"No match found (best score: {best_score:.3f})")
        return None


# Module-level singleton instance
face_recognizer = FaceRecognizer()