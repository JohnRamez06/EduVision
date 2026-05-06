import sys
from pathlib import Path

def _find_project_root():
    p = Path(__file__).resolve().parent
    while p != p.parent:
        if (p / "config.py").exists():
            return p
        p = p.parent
    raise FileNotFoundError("config.py not found — copy config.py.example to config.py")

_root = _find_project_root()
sys.path.insert(0, str(_root))
from config import DB_CONFIG

import cv2
import numpy as np
import mysql.connector
from deepface import DeepFace
import time

FACE_ENROLLMENT_DIR = _root / "face_enrollment"

class FolderEnroller:
    def __init__(self):
        self.model_name = "Facenet"
        
    def get_all_students_from_db(self):
        """Get all students from database that need embeddings"""
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor(dictionary=True)
        
        cursor.execute("""
            SELECT s.user_id, CONCAT(u.first_name, ' ', u.last_name) as full_name
            FROM students s
            JOIN users u ON u.id = s.user_id
            ORDER BY s.user_id
        """)
        
        students = cursor.fetchall()
        cursor.close()
        conn.close()
        return students
    
    def get_photo_for_student(self, student_id):
        """Find photo in face_enrollment folder"""
        student_folder = FACE_ENROLLMENT_DIR / str(student_id)
        
        if not student_folder.exists():
            return None
        
        # Look for jpg, png, jpeg files
        for ext in ['*.jpg', '*.png', '*.jpeg', '*.JPG']:
            photos = list(student_folder.glob(ext))
            if photos:
                return photos[0]
        
        return None
    
    def extract_embedding(self, photo_path):
        """Extract Facenet embedding from photo"""
        try:
            img = cv2.imread(str(photo_path))
            if img is None:
                return None
            
            result = DeepFace.represent(
                img_path=img,
                model_name=self.model_name,
                enforce_detection=True,
                detector_backend="opencv",
                align=True
            )
            
            if result:
                embedding = np.array(result[0]["embedding"], dtype=np.float32)
                # L2 normalize
                embedding = embedding / (np.linalg.norm(embedding) + 1e-9)
                return embedding
                
        except Exception as e:
            print(f"    Error: {str(e)[:100]}")
            return None
        
        return None
    
    def update_embedding_in_db(self, user_id, embedding):
        """Store embedding in database"""
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        embedding_bytes = embedding.tobytes()
        
        query = """
            UPDATE students 
            SET face_encoding = %s 
            WHERE user_id = %s
        """
        cursor.execute(query, (embedding_bytes, user_id))
        conn.commit()
        
        cursor.close()
        conn.close()
    
    def run_enrollment(self):
        print("=" * 70)
        print("📸 STUDENT FACE ENROLLMENT SYSTEM")
        print("=" * 70)
        
        # Get all students
        students = self.get_all_students_from_db()
        print(f"\n📊 Found {len(students)} students in database")
        
        success = []
        failed_no_photo = []
        failed_no_face = []
        
        for idx, student in enumerate(students, 1):
            user_id = student['user_id']
            name = student['full_name']
            
            print(f"\n[{idx}/{len(students)}] Processing: {name} (ID: {user_id})")
            
            # Find photo
            photo_path = self.get_photo_for_student(user_id)
            
            if not photo_path:
                print(f"    ❌ No photo found in face_enrollment/{user_id}/")
                failed_no_photo.append((user_id, name))
                continue
            
            print(f"    📷 Photo: {photo_path.name}")
            
            # Extract embedding
            embedding = self.extract_embedding(photo_path)
            
            if embedding is None:
                print(f"    ❌ No face detected or embedding failed")
                failed_no_face.append((user_id, name))
                continue
            
            if len(embedding) != 128:
                print(f"    ❌ Wrong embedding dimension: {len(embedding)}")
                failed_no_face.append((user_id, name))
                continue
            
            # Save to database
            self.update_embedding_in_db(user_id, embedding)
            print(f"    ✅ SUCCESS! 128-d embedding stored")
            success.append((user_id, name))
            
            # Small delay to avoid overwhelming
            time.sleep(0.1)
        
        # Print summary
        print("\n" + "=" * 70)
        print("📈 ENROLLMENT SUMMARY")
        print("=" * 70)
        print(f"✅ Successfully enrolled: {len(success)} students")
        print(f"❌ No photo found: {len(failed_no_photo)} students")
        print(f"❌ No face detected: {len(failed_no_face)} students")
        
        if failed_no_photo:
            print("\n📁 Students missing photos:")
            for user_id, name in failed_no_photo:
                print(f"   - ID {user_id}: {name}")
        
        if failed_no_face:
            print("\n😞 Students with photos but no face detected:")
            for user_id, name in failed_no_face:
                print(f"   - ID {user_id}: {name}")
                print(f"     → Check photo at: face_enrollment/{user_id}/")
        
        print("\n" + "=" * 70)
        return success, failed_no_photo, failed_no_face

if __name__ == "__main__":
    enroller = FolderEnroller()
    success, no_photo, no_face = enroller.run_enrollment()