import cv2
import numpy as np


class ConcentrationDetector:
    """
    Simple heuristic:
    - Higher sharpness (Laplacian variance) => more "focused"
    This is not real attention tracking, but it is stable and works now.
    """

    def predict(self, face_bgr: np.ndarray) -> dict:
        gray = cv2.cvtColor(face_bgr, cv2.COLOR_BGR2GRAY)
        score = float(cv2.Laplacian(gray, cv2.CV_64F).var())  # typically 0..1000+
        # Normalize roughly
        norm = min(1.0, score / 300.0)

        if norm > 0.75:
            level = "high"
        elif norm > 0.45:
            level = "medium"
        elif norm > 0.20:
            level = "low"
        else:
            level = "distracted"

        return {"level": level, "score": norm}