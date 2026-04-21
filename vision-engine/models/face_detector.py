import cv2
import numpy as np
from pathlib import Path


class FaceDetector:
    """
    Face detector using OpenCV DNN (ResNet SSD).
    Much more accurate than Haar cascades and supports multiple faces well.

    It expects two model files placed in:
      vision-engine/models/pretrained/
        - deploy.prototxt
        - res10_300x300_ssd_iter_140000_fp16.caffemodel

    If you don't have them yet, see the note below.
    """

    def __init__(self, conf_threshold: float = 0.5):
        self.conf_threshold = conf_threshold

        base = Path(__file__).resolve().parent / "pretrained"
        proto = base / "deploy.prototxt"
        model = base / "res10_300x300_ssd_iter_140000_fp16.caffemodel"

        if not proto.exists() or not model.exists():
            raise FileNotFoundError(
                "Missing DNN face detector files. Expected:\n"
                f"  {proto}\n  {model}\n"
                "Download/copy them into vision-engine/models/pretrained/."
            )

        self.net = cv2.dnn.readNetFromCaffe(str(proto), str(model))

    def detect(self, bgr_img: np.ndarray):
        (h, w) = bgr_img.shape[:2]
        blob = cv2.dnn.blobFromImage(
            cv2.resize(bgr_img, (300, 300)),
            1.0,
            (300, 300),
            (104.0, 177.0, 123.0),
        )
        self.net.setInput(blob)
        detections = self.net.forward()

        faces = []
        for i in range(detections.shape[2]):
            conf = float(detections[0, 0, i, 2])
            if conf < self.conf_threshold:
                continue

            box = detections[0, 0, i, 3:7] * np.array([w, h, w, h])
            (x1, y1, x2, y2) = box.astype("int")

            x1 = max(0, x1)
            y1 = max(0, y1)
            x2 = min(w - 1, x2)
            y2 = min(h - 1, y2)

            fw = max(0, x2 - x1)
            fh = max(0, y2 - y1)
            if fw == 0 or fh == 0:
                continue

            faces.append((x1, y1, fw, fh, conf))

        # sort left-to-right for stable ordering
        faces.sort(key=lambda t: t[0])
        # return (x, y, w, h) only
        return [(x, y, fw, fh) for (x, y, fw, fh, _) in faces]