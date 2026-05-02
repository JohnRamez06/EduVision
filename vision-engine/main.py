from fastapi import FastAPI, UploadFile, File, Form
from fastapi.middleware.cors import CORSMiddleware
import numpy as np
import cv2
import httpx
import logging
from datetime import datetime

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

# Store last flush time and sequence index per session
last_flush = {}
seq_counters: dict[str, int] = {}

@app.get("/health")
async def health():
    return {"status": "ok"}

def send_to_spring_boot(session_id: str, result: dict):
    """Send detection data to Spring Boot"""

    emotion_counts = result.get("emotion_counts", {})

    seq_counters[session_id] = seq_counters.get(session_id, 0) + 1

    payload = {
        "sessionId": session_id,
        "seqIndex": seq_counters[session_id],
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
            logger.info(f"Response status: {response.status_code}")
            logger.info(f"Response body: {response.text}")
            
            if response.status_code in [200, 201]:
                logger.info(f"✅ Class snapshot sent!")
            else:
                logger.error(f"❌ Failed: {response.status_code} - {response.text}")
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
        
        # 🔥 NEW: Send to Spring Boot every 10 seconds
        import time
        now = time.time()
        if session_id not in last_flush or (now - last_flush[session_id]) > 10:
            last_flush[session_id] = now
            send_to_spring_boot(session_id, result)

    return {"session_id": session_id, **result}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)