import numpy as np
import cv2

from models.face_detector import FaceDetector
from models.emotion_model import EmotionModel
from models.concentration_detector import ConcentrationDetector
from models.drowsiness_detector import DrowsinessDetector
from models.face_recognizer import face_recognizer

from processors.blink_detector import BlinkDetector
from processors.gaze_estimator import GazeEstimator
from processors.head_pose_estimator import HeadPoseEstimator


class FaceAnalyzer:
    """
    Orchestrates detection + emotion + concentration + additional signals
    for MULTIPLE faces.
    """

    def __init__(self):
        self.face_detector = FaceDetector(conf_threshold=0.0)
        self.emotion_model = EmotionModel()
        self.concentration = ConcentrationDetector()
        self.drowsiness = DrowsinessDetector()

        self.blink = BlinkDetector()
        self.gaze = GazeEstimator()
        self.pose = HeadPoseEstimator()

    def analyze(self, bgr_img: np.ndarray) -> dict:
        faces = self.face_detector.detect(bgr_img)

        people = []
        for idx, (x, y, w, h) in enumerate(faces):
            if w < 30 or h < 30:
                continue

            face = bgr_img[y:y + h, x:x + w]
            if face.size == 0:
                continue

            emo = self.emotion_model.predict(face)
            dominant = max(emo.items(), key=lambda kv: kv[1])[0] if emo else "neutral"
            emo_conf = float(emo.get(dominant, 0.0)) if emo else 0.0

            conc = self.concentration.predict(face)
            drowsy = float(self.drowsiness.predict_score(face))

            blink = self.blink.predict(face)
            gaze = self.gaze.estimate(face)
            pose = self.pose.estimate(bgr_img, {"x": x, "y": y, "w": w, "h": h})

            # 🔥 FACE RECOGNITION
            student_id = None
            student_name = None
            try:
                gray = cv2.cvtColor(face, cv2.COLOR_BGR2GRAY)
                face_resized = cv2.resize(gray, (128, 128))
                embedding = face_resized.flatten().astype(np.float32) / 255.0
                match = face_recognizer.find_best_match(embedding)
                if match:
                    student_id = match["user_id"]
                    student_name = match["student_name"]
                    print(f"👤 RECOGNIZED: {student_name} (ID: {student_id}) - Emotion: {dominant}")
            except Exception as e:
                pass

            people.append(
                {
                    "face_index": idx,
                    "in_class": True,
                    "bounding_box": {"x": int(x), "y": int(y), "w": int(w), "h": int(h)},
                    "emotion_probs": emo,
                    "dominant_emotion": dominant,
                    "emotion_confidence": emo_conf,
                    "concentration": conc,
                    "drowsiness_score": drowsy,
                    "blink": blink,
                    "gaze": gaze,
                    "head_pose": pose,
                    "student_id": student_id,
                    "student_name": student_name,
                }
            )

        return {"people": people}