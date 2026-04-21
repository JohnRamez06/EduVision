import numpy as np

from models.face_detector import FaceDetector
from models.emotion_model import EmotionModel
from models.concentration_detector import ConcentrationDetector
from models.drowsiness_detector import DrowsinessDetector
from models.face_recognizer import FaceRecognizer


class FaceAnalyzer:
    """
    Orchestrates detection + emotion + concentration + embedding generation for MULTIPLE faces.
    """

    def __init__(self):
        self.face_detector = FaceDetector(conf_threshold=0.5)
        self.emotion_model = EmotionModel()
        self.concentration = ConcentrationDetector()
        self.drowsiness = DrowsinessDetector()
        self.recognizer = FaceRecognizer()

    def analyze(self, bgr_img: np.ndarray) -> dict:
        faces = self.face_detector.detect(bgr_img)

        people = []
        for (x, y, w, h) in faces:
            # skip tiny detections (often false positives)
            if w < 40 or h < 40:
                continue

            x2 = x + w
            y2 = y + h
            face = bgr_img[y:y2, x:x2]
            if face.size == 0:
                continue

            emo = self.emotion_model.predict(face)
            dominant = max(emo.items(), key=lambda kv: kv[1])[0] if emo else "neutral"
            conc = self.concentration.predict_level(face)
            drowsy = self.drowsiness.predict_score(face)

            people.append(
                {
                    "bounding_box": {"x": int(x), "y": int(y), "w": int(w), "h": int(h)},
                    "emotion_probs": emo,
                    "dominant_emotion": dominant,
                    "concentration": conc,
                    "drowsiness_score": float(drowsy),
                    "embedding_bytes_len": len(self.recognizer.embedding(face)),
                }
            )

        engagement_score = float(
            sum(p["emotion_probs"].get("engaged", 0.0) for p in people) / max(1, len(people))
        )

        avg_concentration = None
        if people:
            mapping = {"high": 1.0, "medium": 0.66, "low": 0.33, "distracted": 0.0}
            avg_concentration = float(sum(mapping[p["concentration"]] for p in people) / len(people))

        return {
            "student_count": int(len(people)),
            "engagement_score": engagement_score,
            "avg_concentration": avg_concentration,
            "people": people,
        }