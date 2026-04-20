# SINGLETON - only one model instance loaded in memory
class EmotionModel:
    _instance = None
    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._loaded = False
        return cls._instance
    def load(self):
        if not self._loaded:
            # TODO: load FER+ model weights
            self._loaded = True
    def predict(self, face_img) -> dict:
        # TODO: return emotion probabilities
        return {"neutral": 1.0}
