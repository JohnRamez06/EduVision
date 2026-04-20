import cv2, numpy as np

def detect_faces(image_bytes: bytes):
    nparr = np.frombuffer(image_bytes, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
    # TODO: run face detector
    return []
