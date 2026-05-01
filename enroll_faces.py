# enroll_faces.py
import os
import cv2
import numpy as np
import mysql.connector

DB_CONFIG = {
    "host": "localhost",
    "user": "root",
    "password": "",
    "database": "eduvision"
}

BASE_DIR = "face_enrollment"

# Load face detection
face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')

def extract_embedding(image_path):
    img = cv2.imread(image_path)
    if img is None:
        return None
    
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    faces = face_cascade.detectMultiScale(gray, 1.1, 4)
    
    if len(faces) == 0:
        return None
    
    # Get largest face
    x, y, w, h = max(faces, key=lambda f: f[2] * f[3])
    face_roi = gray[y:y+h, x:x+w]
    face_roi = cv2.resize(face_roi, (128, 128))
    
    # Create embedding
    embedding = face_roi.flatten().astype(np.float32) / 255.0
    return embedding.tobytes()

def main():
    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor()
    
    if not os.path.exists(BASE_DIR):
        print(f"❌ Folder not found: {BASE_DIR}")
        return
    
    students = [d for d in os.listdir(BASE_DIR) if os.path.isdir(os.path.join(BASE_DIR, d))]
    total = len(students)
    success = 0
    failed = 0
    no_face = 0
    
    print("=" * 60)
    print(f"EDUVISION - ENROLLING {total} STUDENTS")
    print("=" * 60)
    
    for i, student_id in enumerate(students):
        folder = os.path.join(BASE_DIR, student_id)
        photo_path = os.path.join(folder, "photo_1.jpg")
        
        if not os.path.exists(photo_path):
            print(f"[{i+1}/{total}] ⏭️  {student_id} - No photo")
            failed += 1
            continue
        
        print(f"[{i+1}/{total}] 🔍 {student_id}...", end=" ")
        
        embedding_bytes = extract_embedding(photo_path)
        
        if embedding_bytes:
            try:
                cursor.execute(
                    "UPDATE students SET face_encoding = %s, consent_given = 1, consent_date = NOW() WHERE student_number = %s",
                    (embedding_bytes, student_id)
                )
                conn.commit()
                rows = cursor.rowcount
                if rows > 0:
                    print(f"✅ Enrolled")
                    success += 1
                else:
                    print(f"⚠️ Student not in DB yet - run SQL first")
                    failed += 1
            except Exception as e:
                print(f"❌ Error: {str(e)[:40]}")
                failed += 1
        else:
            print(f"⚠️ No face detected")
            no_face += 1
    
    cursor.close()
    conn.close()
    
    print("=" * 60)
    print(f"RESULTS: {success} enrolled | {no_face} no face | {failed} failed | {total} total")
    print("=" * 60)

if __name__ == "__main__":
    main()