import time
import json
import cv2
from collections import Counter
from datetime import datetime

from config.settings import settings
from database.schema import ensure_schema
from database.repository import ensure_session, insert_detection
from processors.frame_processor import FrameProcessor
from api_client.spring_client import send_snapshot


def dominant_emotion(people):
    if not people:
        return "neutral"
    c = Counter([p.get("dominant_emotion", "neutral") for p in people])
    return c.most_common(1)[0][0]


def avg_concentration(people):
    if not people:
        return 0.0
    # try common fields (adjust if your processor uses different keys)
    vals = []
    for p in people:
        conc = p.get("concentration", {})
        score = conc.get("score")
        if score is None:
            score = conc.get("value")
        if score is None and isinstance(conc, (int, float)):
            score = float(conc)
        if score is not None:
            vals.append(float(score))
    return sum(vals) / max(1, len(vals))


def main():
    # minimal DB tables (sessions/detections) are optional now
    ensure_schema()

    # IMPORTANT: this must be a REAL lecture_sessions.id (UUID)
    session_id = getattr(settings, "active_session_id", None)
    if not session_id:
        session_id = input("Enter ACTIVE lecture_sessions.id (UUID) to store snapshots under: ").strip()

    if getattr(settings, "store_to_minimal_db", False):
        ensure_session(session_id)

    processor = FrameProcessor()

    cap = cv2.VideoCapture(int(getattr(settings, "camera_device_index", 0)))
    if not cap.isOpened():
        raise RuntimeError("Could not open webcam. Close other apps or try another device index.")

    last_store = 0.0
    store_every_seconds = 2.0
    seq_index = 0

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
            cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 255, 0), 2)
            cv2.putText(frame, label, (x, max(0, y - 10)),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 255, 0), 2)

        # store periodically
        now = time.time()
        if now - last_store >= store_every_seconds:
            people = result.get("people", [])
            seq_index += 1

            # Optional: keep minimal detections table for debugging
            if getattr(settings, "store_to_minimal_db", False):
                for det in people:
                    insert_detection(session_id, det)

            if getattr(settings, "store_to_spring", True):
                payload = {
                    # EmotionSnapshotDTO fields (backend expects these names)
                    "snapshotId": None,
                    "sessionId": session_id,
                    "cameraId": None,
                    "seqIndex": seq_index,
                    "capturedAt": datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%S"),
                    "frameUrl": None,
                    "studentCount": int(result.get("student_count", len(people))),
                    "avgConcentration": avg_concentration(people),
                    "dominantEmotion": dominant_emotion(people),
                    # For now, use avgConcentration as engagement_score too
                    "engagementScore": avg_concentration(people),
                    "rawPayload": json.dumps(result),
                    "processingMs": None,
                }

                try:
                    send_snapshot(session_id, payload)
                    print(f"[OK] Sent snapshot seq={seq_index} people={len(people)}")
                except Exception as e:
                    print(f"[ERR] Failed to send snapshot: {e}")

            last_store = now

        cv2.putText(frame, f"Session: {session_id}  Faces: {result.get('student_count', 0)}",
                    (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.75, (255, 255, 255), 2)

        cv2.imshow("EduVision - Webcam (press q to quit)", frame)
        if (cv2.waitKey(1) & 0xFF) in (ord("q"), 27):
            break

    cap.release()
    cv2.destroyAllWindows()


if __name__ == "__main__":
    main()