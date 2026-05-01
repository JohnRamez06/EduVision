from processors.face_analyzer import FaceAnalyzer
from processors.aggregator import Aggregator


class FrameProcessor:
    """
    Facade that runs full per-frame pipeline:
    detect -> analyze faces -> aggregate summary
    """

    def __init__(self):
        self.face_analyzer = FaceAnalyzer()
        self.aggregator = Aggregator()

    def process(self, frame_bgr) -> dict:
        analysis = self.face_analyzer.analyze(frame_bgr)
        summary = self.aggregator.summarize(analysis)
        return {**analysis, **summary}
    
    # frame_processor.py  — only the updated process() method shown;
# merge into your existing class

from models.face_recognizer import face_recognizer   # add to imports at top

# Inside your existing FrameProcessor class:

def process(self, frame, session_id: str) -> list[dict]:
    """
    Detect faces, classify emotions, calculate concentration,
    extract embeddings, identify students.
    Returns list of per-face result dicts.
    """
    results = []

    # 1. Detect faces
    face_locations = self.face_detector.detect(frame)

    for face_loc in face_locations:
        try:
            # 2a. Classify emotion
            emotion, confidence = self.emotion_model.classify(frame, face_loc)

            # 2b. Concentration from gaze / head-pose / blink signals
            concentration = self.concentration_estimator.estimate(frame, face_loc)

            # 2c. Extract face embedding
            face_embedding = self.embedding_extractor.extract(frame, face_loc)

            # 2d. Identify student
            student_id = None
            if face_embedding is not None:
                student_id = face_recognizer.identify_face(face_embedding)

            # 2e. Build result dict
            x1, y1, x2, y2 = face_loc  # adjust to your bounding-box format
            result = {
                "student_id":    student_id,          # str UUID or None
                "emotion":       emotion,              # e.g. "happy"
                "confidence":    round(float(confidence), 4),
                "concentration": round(float(concentration), 4),
                "bounding_box":  {"x1": x1, "y1": y1, "x2": x2, "y2": y2},
            }
            results.append(result)

        except Exception as e:
            logger.warning(f"Error processing face at {face_loc}: {e}")
            continue

    # 3. Pass all results to aggregator
    self.aggregator.collect(session_id, results)

    return results