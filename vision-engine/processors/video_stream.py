import cv2


class VideoStream:
    """
    Wrapper around cv2.VideoCapture for multiple sources:
    - USB webcam: source=0,1,...
    - IP/RTSP: source="rtsp://..."
    - HTTP/MJPEG: source="http://..."
    - Video file: source="file.mp4"
    """

    def __init__(self, source, width=None, height=None):
        self.source = source
        self.cap = cv2.VideoCapture(source)

        if not self.cap.isOpened():
            raise RuntimeError(f"Could not open video source: {source}")

        if width is not None:
            self.cap.set(cv2.CAP_PROP_FRAME_WIDTH, int(width))
        if height is not None:
            self.cap.set(cv2.CAP_PROP_FRAME_HEIGHT, int(height))

    def read(self):
        ok, frame = self.cap.read()
        if not ok:
            return None
        return frame

    def release(self):
        try:
            self.cap.release()
        except Exception:
            pass