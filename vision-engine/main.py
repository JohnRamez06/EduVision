from fastapi import FastAPI, UploadFile, File, Form
from fastapi.middleware.cors import CORSMiddleware
import numpy as np
import cv2
import httpx
import logging
from datetime import datetime
import time

from database.schema import ensure_schema
from database.repository import ensure_session, insert_detection
from processors.frame_processor import FrameProcessor

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Spring Boot URL
SPRING_BOOT_URL = "http://localhost:8080/api/v1/emotion-data"

app = FastAPI(title="EduVision Vision Engine", version="1.0.0")
app.add_middleware(CORSMiddleware, allow_origins=["*"], allow_methods=["*"], allow_headers=["*"])

ensure_schema()
processor = FrameProcessor()

# Store last flush time per session
last_flush = {}

@app.get("/health")
async def health():
    return {"status": "ok"}

def send_to_spring_boot(session_id: str, result: dict):
    """Send detection data to Spring Boot"""

    emotion_counts = result.get("emotion_counts", {})
    people = result.get("people", [])

    payload = {
        "sessionId": session_id,
        "seqIndex": int(time.time() * 1000) % 1000000,
        "capturedAt": datetime.now().strftime("%Y-%m-%dT%H:%M:%S"),
        "studentCount": result.get("student_count", 0),
        "engagementScore": round(float(result.get("engagement_score", 0.5)), 3),
        "dominantEmotion": max(emotion_counts, key=emotion_counts.get) if emotion_counts else "neutral",
        "avgConcentration": round(float(result.get("avg_concentration", 0.5)), 3),
    }
    
    try:
        with httpx.Client(timeout=10.0) as client:
            response = client.post(
                f"{SPRING_BOOT_URL}/class-snapshot",
                json=payload
            )
            
            if response.status_code in [200, 201]:
                snapshot_data = response.json()
                snapshot_id = snapshot_data.get("snapshotId")
                logger.info(f"✅ Class snapshot sent! Students: {result.get('student_count', 0)}")
                
                if people and snapshot_id:
                    student_payload = []
                    recognized = []
                    
                    for person in people:
                        sid = person.get("student_id")
                        # Skip unrecognized students
                        if not sid:
                            continue
                        
                        recognized.append(person.get("student_name", sid))
                        
                        student_payload.append({
                            "id": None,
                            "snapshotId": snapshot_id,
                            "sessionId": session_id,
                            "studentId": sid,
                            "emotion": person.get("dominant_emotion", "neutral"),
                            "concentration": person.get("concentration", {}).get("level", "medium") if isinstance(person.get("concentration"), dict) else "medium",
                            "confidenceScore": float(person.get("emotion_confidence", 0.5)),
                            "capturedAt": datetime.now().strftime("%Y-%m-%dT%H:%M:%S"),
                            "anonymised": False,
                        })
                    
                    if recognized:
                        logger.info(f"👤 Recognized: {', '.join(recognized)}")
                    else:
                        logger.info("👤 No students recognized this frame")
                    
                    if student_payload:
                        student_response = client.post(
                            f"{SPRING_BOOT_URL}/student-snapshots?snapshotId={snapshot_id}",
                            json=student_payload
                        )
                        if student_response.status_code in [200, 201]:
                            logger.info(f"✅ Student snapshots sent ({len(student_payload)} students)")
                        else:
                            logger.warning(f"⚠️ Student snapshots: {student_response.status_code}")
            else:
                logger.error(f"❌ Failed: {response.status_code}")
    except Exception as e:
        logger.error(f"❌ Error: {e}")

@app.post("/analyze/frame")
async def analyze_frame(
    session_id: str = Form(...),
    store: bool = Form(True),
    file: UploadFile = File(...)
):
    data = await file.read()
    nparr = np.frombuffer(data, np.uint8)
    frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
    if frame is None:
        return {"error": "Invalid image"}

    result = processor.process(frame)

    if store:
        ensure_session(session_id)
        for det in result["people"]:
            insert_detection(session_id, det)
        
        now = time.time()
        if session_id not in last_flush or (now - last_flush[session_id]) > 10:
            last_flush[session_id] = now
            send_to_spring_boot(session_id, result)

    return {"session_id": session_id, **result}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)