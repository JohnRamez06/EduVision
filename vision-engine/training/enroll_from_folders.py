# vision-engine/training/enroll_from_folders.py
"""
Enroll all students from face_enrollment folders directly into database
with proper Facenet embeddings (128-dimensions) instead of grayscale pixels.
"""

import sys
import os
import cv2
import numpy as np
import mysql.connector
from pathlib import Path
from tqdm import tqdm
from deepface import DeepFace

# Configuration
FACE_ENROLLMENT_DIR = Path("../face_enrollment")  # Adjust if needed
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': '',
    'database': 'eduvision'
}

class FolderEnroller:
    def __init__(self):
        self.model_name = "Facenet"  # 128-d embeddings
        self.detector_backend = "opencv"
        
    def get_student_list(self):
        """Get all student folders from face_enrollment directory"""
        if not FACE_ENROLLMENT_DIR.exists():
            print(f"❌ Directory not found: {FACE_ENROLLMENT_DIR}")
            return []
        
        student_folders = [d for d in FACE_ENROLLMENT_DIR.iterdir() if d.is_dir()]
        
        # Verify these students exist in database
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        valid_students = []
        for folder in student_folders:
            student_id = folder.name
            cursor.execute("SELECT user_id, CONCAT(first_name, ' ', last_name) FROM students WHERE user_id = %s", (student_id,))
            result = cursor.fetchone()
            if result:
                valid_students.append({
                    'user_id': student_id,
                    'name': result[1],
                    'folder': folder
                })
            else:
                print(f"⚠️ Student ID {student_id} not found in database")
        
        cursor.close()
        conn.close()
        return valid_students
    
    def extract_embedding_from_photo(self, photo_path):
        """Extract Facenet embedding from a single photo"""
        try:
            # Read image
            img = cv2.imread(str(photo_path))
            if img is None:
                return None
            
            # Get embedding using DeepFace
            result = DeepFace.represent(
                img_path=img,
                model_name=self.model_name,
                enforce_detection=True,  # Must detect face
                detector_backend=self.detector_backend,
                align=True  # Align face for better accuracy
            )
            
            if result and len(result) > 0:
                embedding = np.array(result[0]["embedding"], dtype=np.float32)
                # L2 normalize for cosine similarity
                embedding = embedding / np.linalg.norm(embedding)
                return embedding
                
        except Exception as e:
            print(f"  Error: {e}")
            return None
    
    def update_database_embedding(self, user_id, embedding):
        """Store embedding in database as float32 array"""
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        # Convert to bytes for storage
        embedding_bytes = embedding.tobytes()
        
        query = """
            UPDATE students 
            SET face_encoding = %s 
            WHERE user_id = %s
        """
        cursor.execute(query, (embedding_bytes, user_id))
        conn.commit()
        
        # Also store dimension info (optional - for verification)
        cursor.execute("""
            UPDATE students 
            SET updated_at = NOW() 
            WHERE user_id = %s
        """, (user_id,))
        conn.commit()
        
        cursor.close()
        conn.close()
    
    def enroll_all_students(self):
        """Main enrollment process"""
        print("=" * 60)
        print("📸 Face Enrollment from Folder Photos")
        print("=" * 60)
        
        students = self.get_student_list()
        print(f"\n📊 Found {len(students)} students with enrollment photos")
        
        success_count = 0
        fail_count = 0
        
        for student in tqdm(students, desc="Enrolling students"):
            # Find photo in folder
            photo_files = list(student['folder'].glob("*.jpg")) + \
                         list(student['folder'].glob("*.png")) + \
                         list(student['folder'].glob("*.jpeg"))
            
            if not photo_files:
                print(f"\n⚠️ No photo found for {student['name']} (ID: {student['user_id']})")
                fail_count += 1
                continue
            
            # Use first photo (photo_1.jpg)
            photo_path = photo_files[0]
            
            # Extract embedding
            embedding = self.extract_embedding_from_photo(photo_path)
            
            if embedding is not None and len(embedding) == 128:
                self.update_database_embedding(student['user_id'], embedding)
                print(f"\n✅ Enrolled: {student['name']} (ID: {student['user_id']}) - 128-d embedding stored")
                success_count += 1
            else:
                print(f"\n❌ Failed: {student['name']} (ID: {student['user_id']}) - No face detected in {photo_path.name}")
                fail_count += 1
        
        print("\n" + "=" * 60)
        print(f"📈 Enrollment Complete:")
        print(f"   ✅ Success: {success_count} students")
        print(f"   ❌ Failed: {fail_count} students")
        print("=" * 60)
        
        return success_count, fail_count

if __name__ == "__main__":
    enroller = FolderEnroller()
    success, fail = enroller.enroll_all_students()
    
    if fail > 0:
        print(f"\n⚠️ {fail} students need manual photo review")