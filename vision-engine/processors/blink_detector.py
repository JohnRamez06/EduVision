import numpy as np


class BlinkDetector:
    """
    Stub blink detector — returns safe defaults.
    MediaPipe solutions API removed in latest mediapipe;
    replace with landmark-based implementation when needed.
    """

    def __init__(self, ear_threshold: float = 0.20, min_consec_frames: int = 2):
        self.ear_threshold = ear_threshold
        self.min_consec_frames = min_consec_frames

    def predict(self, face_bgr: np.ndarray) -> dict:
        return {"blink": False, "ear": 0.25, "closed": False}
