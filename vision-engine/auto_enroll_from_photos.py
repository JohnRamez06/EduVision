"""
auto_enroll_from_photos.py
--------------------------
Automatically re-generates face embeddings for all students
by downloading their profile_picture_url from the users table.

Usage:
    python auto_enroll_from_photos.py

No webcam needed — uses existing profile photos from the database.
"""

import os
import cv2
import numpy as np
import mysql.connector
import urllib.request
import sys

# ── CONFIG ────────────────────────────────────────────────────────────────────
def _find_project_root():
    from pathlib import Path
    p = Path(__file__).resolve().parent
    while p != p.parent:
        if (p / "config.py").exists():
            return p
        p = p.parent
    raise FileNotFoundError("config.py not found — copy config.py.example to config.py")

sys.path.insert(0, str(_find_project_root()))
from config import DB_CONFIG

cascade = cv2.CascadeClassifier(
    cv2.data.haarcascades + "haarcascade_frontalface_default.xml"
)


def get_db():
    return mysql.connector.connect(**DB_CONFIG)


def fetch_students() -> list[dict]:
    conn = get_db()
    cur  = conn.cursor()
    cur.execute("""
        SELECT s.user_id,
               CONCAT(u.first_name, ' ', u.last_name) AS full_name,
               u.profile_picture_url
        FROM students s
        JOIN users u ON u.id = s.user_id
        WHERE u.deleted_at IS NULL
        ORDER BY u.first_name
    """)
    rows = [
        {"user_id": r[0], "name": r[1], "photo_url": r[2]}
        for r in cur.fetchall()
    ]
    cur.close()
    conn.close()
    return rows


def download_image(url: str) -> np.ndarray | None:
    """Download image from URL and return as BGR numpy array."""
    try:
        req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = resp.read()
        arr = np.frombuffer(data, dtype=np.uint8)
        img = cv2.imdecode(arr, cv2.IMREAD_COLOR)
        return img
    except Exception as e:
        print(f"      ↳ Download failed: {e}")
        return None


def detect_face(img_bgr: np.ndarray):
    """Detect largest face. Returns (x,y,w,h) or None."""
    gray  = cv2.cvtColor(img_bgr, cv2.COLOR_BGR2GRAY)
    faces = cascade.detectMultiScale(
        gray, scaleFactor=1.05, minNeighbors=3, minSize=(30, 30)
    )
    if len(faces) == 0:
        return None
    return tuple(sorted(faces, key=lambda f: f[2] * f[3], reverse=True)[0])


def preprocess_face(face_bgr: np.ndarray) -> np.ndarray | None:
    """Extract 128-d Facenet embedding using DeepFace. Returns normalized float32 array."""
    try:
        from deepface import DeepFace
        result = DeepFace.represent(
            img_path=face_bgr,
            model_name="Facenet",
            enforce_detection=False,
            detector_backend="skip",
            align=True
        )
        if not result:
            return None
        embedding = np.array(result[0]["embedding"], dtype=np.float32)
        norm = np.linalg.norm(embedding)
        if norm > 0:
            embedding = embedding / norm
        return embedding
    except Exception as e:
        print(f"      ↳ DeepFace error: {e}")
        return None


def save_embedding(user_id: str, embedding: np.ndarray) -> bool:
    blob = embedding.astype(np.float32).tobytes()
    conn = get_db()
    cur  = conn.cursor()
    cur.execute(
        "UPDATE students SET face_encoding = %s WHERE user_id = %s",
        (blob, user_id)
    )
    conn.commit()
    ok = cur.rowcount > 0
    cur.close()
    conn.close()
    return ok


def main():
    students = fetch_students()
    if not students:
        print("No students found.")
        sys.exit(1)

    print(f"\n{'='*55}")
    print("   EduVision — Auto Enrollment from Profile Photos")
    print(f"{'='*55}")
    print(f"Found {len(students)} students.\n")

    ok_count      = 0
    skip_no_url   = 0
    skip_no_face  = 0
    fail_count    = 0

    for i, s in enumerate(students, 1):
        name = s["name"]
        uid  = s["user_id"]
        url  = s["photo_url"]

        print(f"[{i:>3}/{len(students)}] {name}")

        if not url or url.strip() == "":
            print("      ↳ ⚠  No profile picture URL — skipping")
            skip_no_url += 1
            continue

        img = download_image(url.strip())
        if img is None:
            fail_count += 1
            continue

        face_box = detect_face(img)
        if face_box is None:
            # Try again with a looser cascade (some photos are close-up)
            gray  = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
            faces = cascade.detectMultiScale(
                gray, scaleFactor=1.01, minNeighbors=1, minSize=(20, 20)
            )
            if len(faces) > 0:
                face_box = tuple(sorted(faces, key=lambda f: f[2]*f[3], reverse=True)[0])

        if face_box is None:
            print("      ↳ ❌  No face detected in photo — skipping")
            skip_no_face += 1
            continue

        x, y, w, h   = face_box
        face_crop    = img[y:y+h, x:x+w]
        embedding    = preprocess_face(face_crop)

        if embedding is None:
            print("      ↳ ❌  Failed to extract Facenet embedding")
            fail_count += 1
            continue

        if save_embedding(uid, embedding):
            print(f"      ↳ ✅  Enrolled (face {w}×{h}px)")
            ok_count += 1
        else:
            print(f"      ↳ ❌  DB save failed")
            fail_count += 1

    print(f"\n{'='*55}")
    print(f"  Done!")
    print(f"  ✅  Enrolled:          {ok_count}")
    print(f"  ⚠   No photo URL:      {skip_no_url}")
    print(f"  ❌  No face detected:  {skip_no_face}")
    print(f"  ❌  Errors:            {fail_count}")
    print(f"{'='*55}")

    if ok_count > 0:
        print("\n  Restart your vision engine to reload the new embeddings.")

    if skip_no_face > 0:
        print(f"\n  Tip: {skip_no_face} students had photos but no face was detected.")
        print("  This usually means the photo is not a clear frontal face.")
        print("  Use run_enrollment.py to manually enroll those students via webcam.")


if __name__ == "__main__":
    main()