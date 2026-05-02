# C:\Users\john\Desktop\eduvision\vision-engine\training\test_single.py
import sys
sys.path.insert(0, r'C:\Users\john\Desktop\eduvision\vision-engine')

import cv2
import numpy as np
from pathlib import Path

# Test with first student folder
student_id = "231002467"
photo_path = Path(f"C:/Users/john/Desktop/eduvision/face_enrollment/{student_id}/photo_1.jpg")

print(f"📸 Testing photo: {photo_path}")
print(f"File exists: {photo_path.exists()}")

if not photo_path.exists():
    print("❌ Photo not found! Check path.")
    exit(1)

# Load image
img = cv2.imread(str(photo_path))
print(f"✅ Image loaded: {img.shape}")

# Test DeepFace
try:
    from deepface import DeepFace
    print("🔄 Extracting embedding...")
    
    result = DeepFace.represent(
        img_path=img,
        model_name="Facenet",
        enforce_detection=True,
        detector_backend="opencv"
    )
    
    embedding = np.array(result[0]["embedding"])
    print(f"✅ SUCCESS! Embedding shape: {embedding.shape}")
    print(f"✅ Expected: 128 dimensions")
    print(f"✅ Norm: {np.linalg.norm(embedding):.3f}")
    
except Exception as e:
    print(f"❌ Failed: {e}")