import cv2
import numpy as np


class FaceDetector:
    """
    Multi-face detector using OpenCV Haar cascade.
    Works out-of-the-box on Windows (no extra model downloads).
    """

    def __init__(self, conf_threshold: float = 0.0):
        self.conf_threshold = conf_threshold
        self.cascade = cv2.CascadeClassifier(
            cv2.data.haarcascades + "haarcascade_frontalface_default.xml"
        )
        if self.cascade.empty():
            raise RuntimeError("Failed to load OpenCV haarcascade_frontalface_default.xml")

    def detect(self, bgr_img: np.ndarray):
        gray = cv2.cvtColor(bgr_img, cv2.COLOR_BGR2GRAY)
        faces = self.cascade.detectMultiScale(
            gray,
            scaleFactor=1.1,
            minNeighbors=3,
            minSize=(30, 30),
        )
        # returns list[(x,y,w,h)]
        faces = [(int(x), int(y), int(w), int(h)) for (x, y, w, h) in faces]
        faces.sort(key=lambda f: f[0])
        return faces