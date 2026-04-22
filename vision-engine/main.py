from fastapi import FastAPI, UploadFile, File, Form
from fastapi.middleware.cors import CORSMiddleware
import numpy as np
import cv2

from database.schema import ensure_schema
from database.repository import ensure_session, insert_detection
from processors.frame_processor import FrameProcessor

app = FastAPI(title="EduVision Vision Engine", version="1.0.0")
app.add_middleware(CORSMiddleware, allow_origins=["*"], allow_methods=["*"], allow_headers=["*"])

ensure_schema()
processor = FrameProcessor()

@app.get("/health")
async def health():
    return {"status": "ok"}

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

    return {"session_id": session_id, **result}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)