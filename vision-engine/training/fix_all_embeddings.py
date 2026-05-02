# C:\Users\john\Desktop\eduvision\vision-engine\training\fix_all_embeddings.py
import sys
sys.path.insert(0, r'C:\Users\john\Desktop\eduvision\vision-engine')

import cv2
import numpy as np
import mysql.connector
from pathlib import Path
from deepface import DeepFace
import time

DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': '',
    'database': 'eduvision'
}

FACE_ENROLLMENT_DIR = Path("C:/Users/john/Desktop/eduvision/face_enrollment")

def get_all_students_with_photos():
    """Get all students that have photos in face_enrollment"""
    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor(dictionary=True)
    
    cursor.execute("""
        SELECT s.user_id, u.first_name, u.last_name, s.student_number
        FROM students s
        JOIN users u ON u.id = s.user_id
        ORDER BY u.last_name
    """)
    
    all_students = cursor.fetchall()
    cursor.close()
    conn.close()
    
    # Find which students have photos
    students_with_photos = []
    
    for student in all_students:
        # Check multiple possible folder names
        possible_folders = [
            str(student['user_id']),
            student['student_number'],
            student['student_number'].replace('STU-', '') if student['student_number'] else None,
        ]
        
        for folder_name in possible_folders:
            if not folder_name:
                continue
                
            folder_path = FACE_ENROLLMENT_DIR / folder_name
            if folder_path.exists():
                photo = list(folder_path.glob("*.jpg")) + list(folder_path.glob("*.png"))
                if photo:
                    students_with_photos.append({
                        'user_id': student['user_id'],
                        'name': f"{student['first_name']} {student['last_name']}",
                        'student_number': student['student_number'],
                        'folder': folder_name,
                        'photo_path': photo[0]
                    })
                    break
    
    return students_with_photos

def extract_facenet_embedding(photo_path):
    """Extract proper 128-d Facenet embedding"""
    try:
        img = cv2.imread(str(photo_path))
        if img is None:
            return None
        
        result = DeepFace.represent(
            img_path=img,
            model_name="Facenet",
            enforce_detection=True,
            detector_backend="opencv",
            align=True
        )
        
        if result and len(result) > 0:
            embedding = np.array(result[0]["embedding"], dtype=np.float32)
            # L2 normalize
            embedding = embedding / (np.linalg.norm(embedding) + 1e-9)
            return embedding
    except Exception as e:
        print(f"      Error: {str(e)[:80]}")
    
    return None

def update_embedding(user_id, embedding):
    """Store 128-d embedding in database"""
    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor()
    
    embedding_bytes = embedding.tobytes()
    
    cursor.execute("""
        UPDATE students 
        SET face_encoding = %s 
        WHERE user_id = %s
    """, (embedding_bytes, user_id))
    
    conn.commit()
    cursor.close()
    conn.close()

def fix_all_embeddings():
    print("=" * 80)
    print("🔧 FIXING ALL EMBEDDINGS - Converting to 128-d Facenet")
    print("=" * 80)
    
    students = get_all_students_with_photos()
    print(f"\n📸 Found {len(students)} students with photos")
    
    success = []
    failed = []
    
    for idx, student in enumerate(students, 1):
        print(f"\n[{idx}/{len(students)}] Processing: {student['name']}")
        print(f"   📁 Folder: {student['folder']}")
        print(f"   📷 Photo: {student['photo_path'].name}")
        
        # Extract embedding
        embedding = extract_facenet_embedding(student['photo_path'])
        
        if embedding is None:
            print(f"   ❌ Failed to extract embedding")
            failed.append(student['name'])
            continue
        
        if len(embedding) != 128:
            print(f"   ❌ Wrong dimension: {len(embedding)}")
            failed.append(student['name'])
            continue
        
        # Update database
        update_embedding(student['user_id'], embedding)
        print(f"   ✅ Success! 128-d embedding stored (norm: {np.linalg.norm(embedding):.3f})")
        success.append(student['name'])
        
        time.sleep(0.1)
    
    # Summary
    print("\n" + "=" * 80)
    print("📈 FIX SUMMARY")
    print("=" * 80)
    print(f"✅ Successfully fixed: {len(success)} students")
    print(f"❌ Failed: {len(failed)} students")
    
    if success:
        print(f"\n✅ Fixed {len(success)} embeddings to 128-d Facenet format")
    
    if failed:
        print(f"\n❌ Failed students:")
        for name in failed:
            print(f"   • {name}")
    
    return success, failed

if __name__ == "__main__":
    success, failed = fix_all_embeddings()
    
    if success:
        print("\n🎯 Next steps:")
        print("1. Restart FastAPI server")
        print("2. Test recognition again")
        print("3. You should see all students loaded")