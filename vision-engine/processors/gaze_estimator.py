import cv2
import numpy as np


class GazeEstimator:
    """
    Heuristic gaze estimator (no face landmarks).
    Estimates gaze direction from brightness centroid in upper face region.

    Output:
      {
        "direction": "center" | "left" | "right" | "unknown",
        "score": float 0..1 (confidence)
      }
    """

    def estimate(self, face_bgr: np.ndarray) -> dict:
        if face_bgr is None or face_bgr.size == 0:
            return {"direction": "unknown", "score": 0.0}

        gray = cv2.cvtColor(face_bgr, cv2.COLOR_BGR2GRAY)
        h, w = gray.shape[:2]

        y1 = int(h * 0.15)
        y2 = int(h * 0.45)
        roi = gray[y1:y2, :]
        if roi.size == 0:
            return {"direction": "unknown", "score": 0.0}

        # Emphasize dark pixels (pupil/iris) by inverting
        inv = 255 - roi
        inv = cv2.GaussianBlur(inv, (7, 7), 0)

        # Compute weighted centroid along x-axis
        weights = inv.astype(np.float32)
        col_sum = weights.sum(axis=0) + 1e-6
        x_coords = np.arange(col_sum.shape[0], dtype=np.float32)
        x_center = float((x_coords * col_sum).sum() / col_sum.sum())  # 0..w

        frac = x_center / max(1.0, float(w))
        # Rough bins
        if frac < 0.42:
            direction = "left"
            score = (0.42 - frac) / 0.42
        elif frac > 0.58:
            direction = "right"
            score = (frac - 0.58) / (1.0 - 0.58)
        else:
            direction = "center"
            score = 1.0 - abs(frac - 0.5) / 0.08

        score = float(max(0.0, min(1.0, score)))
        return {"direction": direction, "score": score}