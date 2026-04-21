import time
import cv2
import numpy as np
from deepface import DeepFace


def analyze_emotion(face_bgr: np.ndarray) -> tuple[str, float]:
    """
    Returns (dominant_emotion, confidence_0_to_1)
    """
    face_rgb = face_bgr[:, :, ::-1]
    res = DeepFace.analyze(face_rgb, actions=["emotion"], enforce_detection=False)
    if isinstance(res, list):
        res = res[0]
    emo = res.get("emotion", {})
    if not emo:
        return ("neutral", 0.0)
    # emo values are percentages
    dominant = max(emo.items(), key=lambda kv: kv[1])[0]
    conf = float(emo[dominant]) / 100.0
    return (dominant, conf)


def main():
    # Haar cascade: works immediately on Windows (no extra model downloads)
    cascade = cv2.CascadeClassifier(cv2.data.haarcascades + "haarcascade_frontalface_default.xml")
    if cascade.empty():
        raise RuntimeError("Failed to load haarcascade_frontalface_default.xml")

    cap = cv2.VideoCapture(0)
    if not cap.isOpened():
        raise RuntimeError("Could not open webcam device 0. Try 1, or close other apps using the camera.")

    # Warm up DeepFace once (first run downloads/loads models; can be slow)
    dummy = np.zeros((224, 224, 3), dtype=np.uint8)
    DeepFace.analyze(dummy, actions=["emotion"], enforce_detection=False)

    last_t = time.time()
    fps = 0.0

    while True:
        ok, frame = cap.read()
        if not ok or frame is None:
            break

        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        faces = cascade.detectMultiScale(gray, scaleFactor=1.1, minNeighbors=5, minSize=(50, 50))

        # Analyze each face
        for (x, y, w, h) in faces:
            face = frame[y : y + h, x : x + w]
            if face.size == 0:
                continue

            dominant, conf = analyze_emotion(face)

            # green box
            cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 255, 0), 2)
            label = f"{dominant} {conf:.2f}"
            cv2.putText(frame, label, (x, max(0, y - 10)),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.65, (0, 255, 0), 2)

        # FPS
        now = time.time()
        dt = now - last_t
        if dt > 0:
            fps = 0.9 * fps + 0.1 * (1.0 / dt)
        last_t = now

        cv2.putText(frame, f"Faces: {len(faces)}  FPS: {fps:.1f}",
                    (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (255, 255, 255), 2)

        cv2.imshow("EduVision - DeepFace Webcam Demo (press q to quit)", frame)
        if (cv2.waitKey(1) & 0xFF) in (ord("q"), 27):
            break

    cap.release()
    cv2.destroyAllWindows()


if __name__ == "__main__":
    main()