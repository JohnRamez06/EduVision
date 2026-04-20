import numpy as np

def classify_frame(image_bytes: bytes) -> dict:
    # TODO: integrate DeepFace / custom model
    return {
        "dominant_emotion": "neutral",
        "concentration": "medium",
        "engagement_score": 0.5,
        "confidence": 0.85,
        "student_count": 0
    }
