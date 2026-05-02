# vision-engine/training/test_single_student.py
import cv2
import numpy as np
from pathlib import Path
from deepface import DeepFace

# Test with a single student
student_id = "231002467"  # First student folder
photo_path = Path(f"../face_enrollment/{student_id}/photo_1.jpg")

# Load and check image
img = cv2.imread(str(photo_path))
print(f"Image shape: {img.shape}")
print(f"Image dtype: {img.dtype}")

# Extract embedding
result = DeepFace.represent(
    img_path=img,
    model_name="Facenet",
    enforce_detection=True,
    detector_backend="opencv"
)

embedding = np.array(result[0]["embedding"])
print(f"✅ Embedding shape: {embedding.shape}")
print(f"✅ Embedding sample: {embedding[:5]}")
print(f"✅ Norm: {np.linalg.norm(embedding)}")