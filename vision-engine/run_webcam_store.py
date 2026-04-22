import time
import cv2

from config.settings import settings
from database.schema import ensure_schema
from database.repository import ensure_session, insert_detection
from processors.frame_processor import FrameProcessor


def main():
    ensure_schema()
    session_id = time.strftime("webcam-%Y%m%d-%H%M%S")
    ensure_session(session_id)

    processor = FrameProcessor()

    cap = cv2.VideoCapture(int(getattr(settings, "camera_device_index", 0)))
    if not cap.isOpened():
        raise RuntimeError("Could not open webcam. Close other apps or try another device index.")

    last_store = 0.0
    store_every_seconds = 2.0

    while True:
        ok, frame = cap.read()
        if not ok or frame is None:
            break

        result = processor.process(frame)

        # draw green boxes
        for p in result["people"]:
            b = p["bounding_box"]
            x, y, w, h = b["x"], b["y"], b["w"], b["h"]
            blink = p.get("blink", {})
            gaze = p.get("gaze", {})
            pose = p.get("head_pose", {})

            label = (
                f'{p["dominant_emotion"]} {p["emotion_confidence"]:.2f} | '
                f'{p["concentration"]["level"]} | '
                f'blink:{int(bool(blink.get("blink")))} '
                f'gaze:{gaze.get("direction","?")} '
                f'yaw:{pose.get("yaw",0.0):.0f}'
            )
            cv2.rectangle(frame, (x, y), (x+w, y+h), (0, 255, 0), 2)
            cv2.putText(frame, label, (x, max(0, y-10)),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 255, 0), 2)

        # store periodically
        now = time.time()
        if now - last_store >= store_every_seconds:
            for det in result["people"]:
                insert_detection(session_id, det)
            last_store = now

        cv2.putText(frame, f"Session: {session_id}  Faces: {result['student_count']}",
                    (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.75, (255, 255, 255), 2)

        cv2.imshow("EduVision - Webcam (press q to quit)", frame)
        if (cv2.waitKey(1) & 0xFF) in (ord("q"), 27):
            break

    cap.release()
    cv2.destroyAllWindows()


if __name__ == "__main__":
    main()