from fastapi import APIRouter, UploadFile, File, HTTPException
from app.services.emotion_classifier import classify_frame
router = APIRouter()

@router.post("/analyze")
async def analyze_frame(session_id: str, file: UploadFile = File(...)):
    contents = await file.read()
    result = classify_frame(contents)
    return {"session_id": session_id, "analysis": result}
