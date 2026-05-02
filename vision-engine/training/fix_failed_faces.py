# C:\Users\john\Desktop\eduvision\vision-engine\training\fix_failed_faces.py
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

# Students that failed with no face detected
FAILED_STUDENTS = [
    {"folder": "231004206", "name": "ريم حسين حسن"},
    {"folder": "231005915", "name": "احمد فوزى الياسرجى"},
    {"folder": "231006507", "name": "ندى شريف ابراهيم"},
]

def get_student_by_name(name):
    """Find student in database by name"""
    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor(dictionary=True)
    
    # Try exact match first
    cursor.execute("""
        SELECT s.user_id, u.first_name, u.last_name, s.student_number
        FROM students s
        JOIN users u ON u.id = s.user_id
        WHERE CONCAT(u.first_name, ' ', u.last_name) = %s
    """, (name,))
    
    student = cursor.fetchone()
    
    if not student:
        # Try partial match
        first_name = name.split()[0]
        cursor.execute("""
            SELECT s.user_id, u.first_name, u.last_name, s.student_number
            FROM students s
            JOIN users u ON u.id = s.user_id
            WHERE u.first_name LIKE %s OR u.last_name LIKE %s
        """, (f"%{first_name}%", f"%{name.split()[-1]}%"))
        student = cursor.fetchone()
    
    cursor.close()
    conn.close()
    return student

def extract_embedding_aggressive(photo_path):
    """Try harder to extract face with multiple methods"""
    
    # Method 1: Direct DeepFace with different detectors
    for detector in ["opencv", "mtcnn"]:
        try:
            img = cv2.imread(str(photo_path))
            if img is None:
                continue
                
            # Try with different preprocessing
            result = DeepFace.represent(
                img_path=img,
                model_name="Facenet",
                enforce_detection=False,  # Don't require face detection
                detector_backend=detector,
                align=True
            )
            
            if result and len(result) > 0:
                embedding = np.array(result[0]["embedding"], dtype=np.float32)
                embedding = embedding / (np.linalg.norm(embedding) + 1e-9)
                print(f"      ✅ Detected with {detector} backend")
                return embedding
        except Exception as e:
            continue
    
    # Method 2: Manual face detection + crop
    try:
        img = cv2.imread(str(photo_path))
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        
        face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')
        faces = face_cascade.detectMultiScale(gray, 1.1, 5)
        
        if len(faces) > 0:
            x, y, w, h = faces[0]
            face_crop = img[y:y+h, x:x+w]
            
            # Try DeepFace on cropped face
            result = DeepFace.represent(
                img_path=face_crop,
                model_name="Facenet",
                enforce_detection=False,
                detector_backend="skip",  # Skip detection, already cropped
                align=True
            )
            
            if result:
                embedding = np.array(result[0]["embedding"], dtype=np.float32)
                embedding = embedding / (np.linalg.norm(embedding) + 1e-9)
                print(f"      ✅ Detected with manual crop")
                return embedding
    except:
        pass
    
    return None

def fix_failed_students():
    print("=" * 80)
    print("🔧 FIXING STUDENTS WITH NO FACE DETECTED")
    print("=" * 80)
    
    success_count = 0
    
    for student_info in FAILED_STUDENTS:
        folder = student_info["folder"]
        name = student_info["name"]
        
        print(f"\n📂 Processing: {name} (Folder: {folder})")
        
        # Find student in database
        student = get_student_by_name(name)
        
        if not student:
            print(f"   ❌ Could not find student in database: {name}")
            continue
        
        print(f"   👤 Found: {student['first_name']} {student['last_name']} (ID: {student['user_id']})")
        
        # Find photo
        folder_path = FACE_ENROLLMENT_DIR / folder
        if not folder_path.exists():
            print(f"   ❌ Folder not found: {folder_path}")
            continue
        
        photos = list(folder_path.glob("*.jpg")) + list(folder_path.glob("*.png"))
        if not photos:
            print(f"   ❌ No photo found")
            continue
        
        photo_path = photos[0]
        print(f"   📷 Trying to extract face from: {photo_path.name}")
        
        # Try aggressive extraction
        embedding = extract_embedding_aggressive(photo_path)
        
        if embedding is None:
            print(f"   ❌ Still cannot detect face. Photo may be corrupted or no face visible.")
            print(f"   💡 Suggestion: Replace photo in {folder_path} with a clear frontal face photo")
            continue
        
        # Update database
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor()
        embedding_bytes = embedding.tobytes()
        cursor.execute("UPDATE students SET face_encoding = %s WHERE user_id = %s", 
                       (embedding_bytes, student['user_id']))
        conn.commit()
        cursor.close()
        conn.close()
        
        print(f"   ✅ SUCCESS! Embedding stored (norm: {np.linalg.norm(embedding):.3f})")
        success_count += 1
    
    print("\n" + "=" * 80)
    print(f"✅ Fixed: {success_count}/{len(FAILED_STUDENTS)} students")
    print("=" * 80)
    
    return success_count

if __name__ == "__main__":
    fix_failed_students()