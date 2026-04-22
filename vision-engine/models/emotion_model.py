import numpy as np
from deepface import DeepFace


class EmotionModel:
    """
    Singleton DeepFace emotion model wrapper.
    """
    _instance = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._warmed = False
        return cls._instance

    def warmup(self):
        if self._warmed:
            return
        dummy = np.zeros((224, 224, 3), dtype=np.uint8)
        DeepFace.analyze(dummy, actions=["emotion"], enforce_detection=False)
        self._warmed = True

    def predict(self, face_bgr) -> dict:
        if not self._warmed:
            self.warmup()

        face_rgb = face_bgr[:, :, ::-1]
        res = DeepFace.analyze(face_rgb, actions=["emotion"], enforce_detection=False)
        if isinstance(res, list):
            res = res[0]

        emotions = res.get("emotion", {})  # percentages
        probs = {k: float(v) / 100.0 for k, v in emotions.items()}

        # Map DeepFace keys to your app keys
        return {
            "happy": probs.get("happy", 0.0),
            "sad": probs.get("sad", 0.0),
            "angry": probs.get("angry", 0.0),
            "surprised": probs.get("surprise", 0.0),
            "fearful": probs.get("fear", 0.0),
            "disgusted": probs.get("disgust", 0.0),
            "neutral": probs.get("neutral", 0.0),
            "confused": 0.0,
            "engaged": 0.0,
        }