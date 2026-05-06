# enroll_faces.py
import os
import cv2
import numpy as np
import mysql.connector

def _find_project_root():
    from pathlib import Path
    p = Path(__file__).resolve().parent
    while p != p.parent:
        if (p / "config.py").exists():
            return p
        p = p.parent
    raise FileNotFoundError("config.py not found — copy config.py.example to config.py")

import sys as _sys
_sys.path.insert(0, str(_find_project_root()))
from config import DB_CONFIG

BASE_DIR = "face_enrollment"

face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')


def extract_embedding(image_path):
    """Extract 128-d Facenet embedding (512 bytes) using DeepFace."""
    try:
        from deepface import DeepFace
        import sys
        sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'vision-engine'))

        img = cv2.imread(image_path)
        if img is None:
            return None

        result = DeepFace.represent(
            img_path=image_path,
            model_name="Facenet",
            enforce_detection=False,
            align=True
        )

        if not result or len(result) == 0:
            return None

        embedding = np.array(result[0]["embedding"], dtype=np.float32)
        # Normalize to unit vector
        norm = np.linalg.norm(embedding)
        if norm > 0:
            embedding = embedding / norm

        return embedding.tobytes()  # 128 * 4 = 512 bytes

    except Exception as e:
        print(f"DeepFace error: {e}")
        return None


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
