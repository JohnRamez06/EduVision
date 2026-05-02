"""
face_recognizer.py - FIXED VERSION
Directly uses stored 128-d Facenet embeddings from database
No conversion to images - just cosine similarity on embeddings
"""
import logging
import numpy as np
import mysql.connector
from typing import Optional, Dict, List

logger = logging.getLogger(__name__)

# Configuration
THRESHOLD = 0.40  # Cosine similarity threshold (0.4 = good balance)

class FaceRecognizer:
    """
    Singleton that loads 128-d Facenet embeddings directly from database
    and uses cosine similarity for fast, accurate face matching.
    """
    
    _instance = None
    
    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._db = []
            cls._instance._loaded = False
        return cls._instance
    
    def _get_db_connection(self):
        """Get MySQL connection"""
        try:
            from database.mysql_connector import get_connection
            return get_connection()
        except:
            import mysql.connector
            return mysql.connector.connect(
                host='localhost',
                user='root',
                password='',
                database='eduvision'
            )
    
    def load(self):
        """Load all face embeddings directly from database"""
        logger.info("Loading face embeddings from database...")
        
        try:
            conn = self._get_db_connection()
            cursor = conn.cursor(dictionary=True)
            
            # Get all students with valid 512-byte embeddings (128 floats * 4 bytes)
            cursor.execute("""
                SELECT s.user_id, 
                       CONCAT(u.first_name, ' ', u.last_name) AS full_name,
                       s.face_encoding,
                       LENGTH(s.face_encoding) as encoding_size
                FROM students s
                JOIN users u ON u.id = s.user_id
                WHERE s.face_encoding IS NOT NULL 
                  AND LENGTH(s.face_encoding) = 512
            """)
            
            students = cursor.fetchall()
            cursor.close()
            conn.close()
            
            logger.info(f"Found {len(students)} students with valid embeddings")
            
            loaded_count = 0
            self._db = []
            
            for student in students:
                try:
                    # Convert bytes to numpy array of float32
                    embedding = np.frombuffer(student['face_encoding'], dtype=np.float32)
                    
                    # Verify it's 128-dimensional
                    if len(embedding) != 128:
                        logger.warning(f"  Skipping {student['full_name']}: wrong dimension {len(embedding)}")
                        continue
                    
                    # Check if embedding is normalized (norm should be ~1.0)
                    norm = np.linalg.norm(embedding)
                    if abs(norm - 1.0) > 0.1:
                        # Re-normalize just in case
                        embedding = embedding / (norm + 1e-9)
                        logger.debug(f"  Re-normalized {student['full_name']} (norm was {norm:.3f})")
                    
                    self._db.append({
                        "user_id": student['user_id'],
                        "full_name": student['full_name'],
                        "vector": embedding
                    })
                    loaded_count += 1
                    logger.info(f"  ✓ Loaded: {student['full_name']}")
                    
                except Exception as e:
                    logger.warning(f"  ✗ Failed to load {student['full_name']}: {e}")
            
            self._loaded = True
            logger.info(f"✅ FaceRecognizer ready — {loaded_count} student(s) enrolled")
            
        except Exception as e:
            logger.error(f"Database error: {e}")
            self._loaded = False
    
    def reload(self):
        """Reload embeddings from database"""
        self._loaded = False
        self.load()
    
    def _cosine_similarity(self, a: np.ndarray, b: np.ndarray) -> float:
        """Calculate cosine similarity between two vectors"""
        dot = np.dot(a, b)
        norm_a = np.linalg.norm(a)
        norm_b = np.linalg.norm(b)
        
        if norm_a == 0 or norm_b == 0:
            return 0.0
        
        return float(dot / (norm_a * norm_b))
    
    def find_best_match(self, face_bgr: np.ndarray) -> Optional[Dict]:
        """
        Match a face crop against enrolled students.
        
        Args:
            face_bgr: BGR image of the face (numpy array)
            
        Returns:
            Dict with user_id, student_name, similarity or None if no match
        """
        if not self._loaded:
            self.load()
        
        if not self._db:
            logger.warning("No students enrolled in recognizer")
            return None
        
        # Extract embedding from the face crop
        try:
            from deepface import DeepFace
            
            # DeepFace expects RGB, convert from BGR
            face_rgb = cv2.cvtColor(face_bgr, cv2.COLOR_BGR2RGB)
            
            result = DeepFace.represent(
                img_path=face_rgb,
                model_name="Facenet",
                enforce_detection=False,  # Face already detected
                detector_backend="skip",   # Skip detection, we already have the face
                align=True
            )
            
            if not result or len(result) == 0:
                logger.debug("Failed to extract embedding from face")
                return None
            
            query_embedding = np.array(result[0]["embedding"], dtype=np.float32)
            # Normalize
            query_embedding = query_embedding / (np.linalg.norm(query_embedding) + 1e-9)
            
        except Exception as e:
            logger.debug(f"DeepFace error: {e}")
            return None
        
        # Find best match
        best_score = -1.0
        best_match = None
        
        for entry in self._db:
            score = self._cosine_similarity(query_embedding, entry["vector"])
            
            if score > best_score:
                best_score = score
                best_match = entry
        
        # Check against threshold
        if best_match and best_score >= THRESHOLD:
            logger.info(f"👤 {best_match['full_name']} (sim={best_score:.3f})")
            return {
                "user_id": best_match["user_id"],
                "student_name": best_match["full_name"],
                "similarity": best_score
            }
        
        logger.debug(f"No match (best similarity: {best_score:.3f})")
        return None
    
    def identify_face(self, face_bgr: np.ndarray) -> Optional[str]:
        """Return user_id if recognized, else None"""
        match = self.find_best_match(face_bgr)
        return match["user_id"] if match else None
    
    def get_enrolled_count(self) -> int:
        """Return number of enrolled students"""
        if not self._loaded:
            self.load()
        return len(self._db)


# Module-level singleton
face_recognizer = FaceRecognizer()

# Import cv2 locally to avoid circular imports
import cv2