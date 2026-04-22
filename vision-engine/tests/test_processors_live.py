import cv2
from processors.frame_processor import FrameProcessor

def main():
    cap = cv2.VideoCapture(0)
    if not cap.isOpened():
        raise RuntimeError("Cannot open camera")

    processor = FrameProcessor()

    ok, frame = cap.read()
    cap.release()
    if not ok:
        raise RuntimeError("Cannot read frame")

    result = processor.process(frame)

    print("student_count:", result.get("student_count"))
    people = result.get("people", [])
    for i, p in enumerate(people):
        print(f"\n--- Person {i} ---")
        print("dominant_emotion:", p.get("dominant_emotion"), "conf:", p.get("emotion_confidence"))
        print("concentration:", p.get("concentration"))
        print("blink:", p.get("blink"))
        print("gaze:", p.get("gaze"))
        print("head_pose:", p.get("head_pose"))

if __name__ == "__main__":
    main()