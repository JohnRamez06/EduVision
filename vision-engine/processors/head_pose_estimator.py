import cv2
import numpy as np


class HeadPoseEstimator:
    """
    Heuristic head pose estimator without landmarks.

    Uses face bounding box aspect ratio + position as proxy:
    - Wide faces can suggest yaw (turned), but this is rough.
    - Returns yaw/pitch/roll as approximations.

    Output:
      {
        "yaw": float degrees,
        "pitch": float degrees,
        "roll": float degrees,
        "score": float 0..1
      }
    """

    def estimate(self, frame_bgr: np.ndarray, box: dict) -> dict:
        # box = {"x","y","w","h"}
        try:
            w = float(box["w"])
            h = float(box["h"])
        except Exception:
            return {"yaw": 0.0, "pitch": 0.0, "roll": 0.0, "score": 0.0}

        if w <= 0 or h <= 0:
            return {"yaw": 0.0, "pitch": 0.0, "roll": 0.0, "score": 0.0}

        ar = w / h  # aspect ratio

        # Neutral face often around 0.75-0.95 depending on crop.
        # We'll map deviation to yaw.
        yaw = (ar - 0.85) * 60.0  # rough scale
        yaw = float(max(-45.0, min(45.0, yaw)))

        # Pitch/roll unknown without landmarks; keep 0 but provide score.
        score = float(max(0.0, min(1.0, 1.0 - abs(yaw) / 60.0)))

        return {"yaw": yaw, "pitch": 0.0, "roll": 0.0, "score": score}