# models/face_recognizer.py
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

# Lower threshold for grayscale flatten embeddings
SIMILARITY_THRESHOLD = 0.3


def load_embeddings() -> list[dict]:
    """Load all students with non-null face encodings from MySQL."""
    embeddings = []
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor()
        cursor.execute(
            "SELECT user_id, face_encoding FROM students WHERE face_encoding IS NOT NULL"
        )
        rows = cursor.fetchall()
        for user_id, blob in rows:
            try:
                vector = np.frombuffer(blob, dtype=np.float32)
                if len(vector) > 0:
                    embeddings.append({"user_id": user_id, "embedding_vector": vector})
            except Exception as e:
                logger.warning(f"Failed to decode embedding for user {user_id}: {e}")
        cursor.close()
        conn.close()
        logger.info(f"Loaded {len(embeddings)} face embeddings from database.")
    except mysql.connector.Error as e:
        logger.error(f"Database error loading embeddings: {e}")
    return embeddings


def _cosine_similarity(vec_a: np.ndarray, vec_b: np.ndarray) -> float:
    """Calculate cosine similarity between two vectors."""
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
        """Load embeddings from database."""
        self._embeddings = load_embeddings()
        self._loaded = True
        logger.info(f"FaceRecognizer loaded with {len(self._embeddings)} embeddings")

    def reload(self):
        """Refresh embeddings from DB (call when new students enroll)."""
        self.load()

    def identify_face(self, face_embedding: np.ndarray) -> Optional[str]:
        """Compare face_embedding against stored embeddings. Returns user_id or None."""
        if not self._loaded:
            self.load()

        if len(self._embeddings) == 0:
            return None

        best_id = None
        best_score = -1.0

        for entry in self._embeddings:
            score = _cosine_similarity(face_embedding, entry["embedding_vector"])
            if score > best_score:
                best_score = score
                best_id = entry["user_id"]

        if best_score >= SIMILARITY_THRESHOLD:
            logger.info(f"Identified student {best_id} with similarity {best_score:.3f}")
            return best_id

        logger.debug(f"No match found (best score: {best_score:.3f})")
        return None

    def find_best_match(self, face_embedding: np.ndarray) -> Optional[dict]:
        """Find the best matching student. Returns dict with user_id, student_name, similarity."""
        if not self._loaded:
            self.load()

        if len(self._embeddings) == 0:
            return None

        best_entry = None
        best_score = -1.0

        for entry in self._embeddings:
            score = _cosine_similarity(face_embedding, entry["embedding_vector"])
            if score > best_score:
                best_score = score
                best_entry = entry

        if best_score >= SIMILARITY_THRESHOLD:
            student_name = self._get_student_name(best_entry["user_id"])
            return {
                "user_id": best_entry["user_id"],
                "student_name": student_name,
                "similarity": best_score,
            }

        return None

    def _get_student_name(self, user_id: str) -> str:
        """Get student name from database."""
        try:
            conn = mysql.connector.connect(**DB_CONFIG)
            cursor = conn.cursor()
            cursor.execute(
                "SELECT CONCAT(first_name, ' ', last_name) FROM users WHERE id = %s",
                (user_id,)
            )
            row = cursor.fetchone()
            cursor.close()
            conn.close()
            return row[0] if row else "Unknown"
        except Exception:
            return "Unknown"


# Module-level singleton instance
face_recognizer = FaceRecognizer()