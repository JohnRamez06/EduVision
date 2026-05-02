"""
face_recognizer.py
──────────────────
Loads face_encoding blobs from students table, reconstructs the 128×128
grayscale face images stored in them, then computes proper Facenet embeddings
via DeepFace — giving accurate, discriminative face recognition.

On matching: incoming live BGR crops are also embedded with Facenet and
compared using cosine similarity.
"""
import logging
from typing import Optional

import cv2
import numpy as np

from database.mysql_connector import get_connection

logger = logging.getLogger(__name__)

# ── Config ─────────────────────────────────────────────────────────────────────

STORED_PIXELS   = 128 * 128          # 16 384 float32 values per enrolled face
DEEPFACE_MODEL  = "Facenet"          # 128-d embeddings
THRESHOLD       = 0.40               # cosine similarity cutoff for Facenet


# ── Facenet via DeepFace ────────────────────────────────────────────────────────

def _embed_bgr(img_bgr: np.ndarray) -> Optional[np.ndarray]:
    """Compute a Facenet 128-d embedding from a BGR image."""
    try:
        from deepface import DeepFace
        result = DeepFace.represent(
            img_path=img_bgr,
            model_name=DEEPFACE_MODEL,
            enforce_detection=False,
            detector_backend="opencv",
        )
        if result:
            return np.array(result[0]["embedding"], dtype=np.float32)
    except Exception as e:
        logger.warning(f"DeepFace.represent error: {e}")
    return None


def _blob_to_bgr(blob: bytes) -> Optional[np.ndarray]:
    """
    Reconstruct a BGR image from a stored face_encoding blob.
    The blob is a float32 128×128 grayscale pixel array (values 0-1).
    """
    try:
        vec = np.frombuffer(blob, dtype=np.float32)
        if len(vec) != STORED_PIXELS:
            return None
        gray = (vec * 255).astype(np.uint8).reshape(128, 128)
        return cv2.cvtColor(gray, cv2.COLOR_GRAY2BGR)
    except Exception as e:
        logger.warning(f"Blob decode error: {e}")
        return None


# ── DB ─────────────────────────────────────────────────────────────────────────

def _load_students() -> list[dict]:
    rows = []
    try:
        conn = get_connection()
        cur  = conn.cursor()
        cur.execute("""
            SELECT s.user_id,
                   CONCAT(u.first_name, ' ', u.last_name) AS full_name,
                   s.face_encoding
            FROM   students s
            JOIN   users    u ON u.id = s.user_id
            WHERE  s.face_encoding IS NOT NULL
        """)
        for uid, name, blob in cur.fetchall():
            rows.append({"user_id": uid, "full_name": name, "blob": blob})
        cur.close()
        conn.close()
        logger.info(f"Fetched {len(rows)} students with face_encoding from DB")
    except Exception as e:
        logger.error(f"DB error: {e}")
    return rows


# ── Cosine similarity ───────────────────────────────────────────────────────────

def _cosine(a: np.ndarray, b: np.ndarray) -> float:
    na, nb = np.linalg.norm(a), np.linalg.norm(b)
    return float(np.dot(a, b) / (na * nb)) if na > 0 and nb > 0 else 0.0


# ── Recognizer ──────────────────────────────────────────────────────────────────

class FaceRecognizer:
    """
    Singleton. On load():
      1. Fetches all student face_encoding blobs from MySQL.
      2. Reconstructs each blob into a 128×128 grayscale image.
      3. Computes a Facenet embedding for each image via DeepFace.
      4. Keeps those embeddings in memory for cosine-similarity lookup.

    find_best_match(face_bgr) embeds the incoming crop and returns the
    closest enrolled student above THRESHOLD, or None.
    """

    _instance = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._db     = []
            cls._instance._loaded = False
        return cls._instance

    def load(self):
        students = _load_students()
        db = []
        for s in students:
            img = _blob_to_bgr(s["blob"])
            if img is None:
                logger.debug(f"Skipping {s['full_name']}: wrong blob size")
                continue
            vec = _embed_bgr(img)
            if vec is None:
                logger.warning(f"  ✗ {s['full_name']}: embedding failed")
                continue
            db.append({
                "user_id":   s["user_id"],
                "full_name": s["full_name"],
                "vector":    vec,
            })
            logger.info(f"  ✓ Enrolled: {s['full_name']}")

        self._db     = db
        self._loaded = True
        logger.info(f"FaceRecognizer ready — {len(db)} student(s) enrolled")

    def reload(self):
        self._loaded = False
        self.load()

    def find_best_match(self, face_bgr: np.ndarray) -> Optional[dict]:
        """Match a BGR face crop → {user_id, student_name, similarity} or None."""
        if not self._loaded:
            self.load()

        if not self._db:
            logger.warning("No students enrolled")
            return None

        query = _embed_bgr(face_bgr)
        if query is None:
            return None

        best, best_score = None, -1.0
        for entry in self._db:
            score = _cosine(query, entry["vector"])
            if score > best_score:
                best_score = score
                best = entry

        if best and best_score >= THRESHOLD:
            logger.info(f"👤 {best['full_name']} (sim={best_score:.3f})")
            return {
                "user_id":      best["user_id"],
                "student_name": best["full_name"],
                "similarity":   best_score,
            }

        logger.debug(f"No match (best={best_score:.3f})")
        return None

    def identify_face(self, face_bgr: np.ndarray) -> Optional[str]:
        m = self.find_best_match(face_bgr)
        return m["user_id"] if m else None


# Module-level singleton
face_recognizer = FaceRecognizer()
