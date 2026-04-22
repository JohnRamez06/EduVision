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