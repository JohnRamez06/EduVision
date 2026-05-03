# C:\Users\john\Desktop\eduvision\vision-engine\main.py

from fastapi import FastAPI, UploadFile, File, Form
from fastapi.middleware.cors import CORSMiddleware
import numpy as np
import cv2
import httpx
import logging
from datetime import datetime
import time
import asyncio
import threading
from collections import defaultdict
from datetime import datetime, timedelta
from database.schema import ensure_schema
from database.repository import ensure_session, insert_detection
from processors.frame_processor import FrameProcessor

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Spring Boot URL
SPRING_BOOT_URL = "http://localhost:8080/api/v1/emotion-data"
ATTENDANCE_URL = "http://localhost:8080/api/v1/attendance"

app = FastAPI(title="EduVision Vision Engine", version="1.0.0")
student_appearances = defaultdict(lambda: defaultdict(int))
student_confidence = defaultdict(lambda: defaultdict(list))
ATTENDANCE_THRESHOLD = 3  # Minimum appearances required
SIMILARITY_THRESHOLD = 0.6  # Minimum similarity score (0.6 = 60%)
ATTENDANCE_WINDOW_SECONDS = 30  # Time window to count multiple detections
enrollment_cache = {}
ENROLLMENT_CACHE_TTL = 300  # 5 minutes

# CORS Configuration - Allow all for development
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000", "http://127.0.0.1:3000", "http://localhost:8080"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize database schema
try:
    ensure_schema()
    logger.info("✅ Database schema ready")
except Exception as e:
    logger.warning(f"Database schema warning: {e}")

processor = FrameProcessor()

# Pre-load face embeddings at startup
from models.face_recognizer import face_recognizer as _fr
try:
    _fr.load()
    logger.info(f"✅ Face recognizer loaded with {len(_fr._db)} students")
except Exception as _e:
    logger.warning(f"Face embeddings pre-load failed: {_e}")

# Store last flush time per session
last_flush = {}
current_session_id = None

def should_mark_attendance(session_id: str, student_id: str, similarity: float) -> bool:
    """
    Determine if a student should be marked as present based on:
    1. Similarity score above threshold
    2. Multiple appearances within time window
    """
    global student_appearances, student_confidence
    
    # Check similarity threshold
    if similarity < SIMILARITY_THRESHOLD:
        logger.debug(f"⏭️ Skipping {student_id[:8]}... similarity too low: {similarity:.3f}")
        return False
    
    # Track appearance
    now = datetime.now()
    student_appearances[session_id][student_id] += 1
    student_confidence[session_id][student_id].append(similarity)
    
    appearances = student_appearances[session_id][student_id]
    
    # Log progress
    logger.debug(f"📊 {student_id[:8]}... appearances: {appearances}/{ATTENDANCE_THRESHOLD} (sim: {similarity:.3f})")
    
    # Check if reached threshold
    if appearances >= ATTENDANCE_THRESHOLD:
        # Calculate average confidence
        avg_confidence = sum(student_confidence[session_id][student_id]) / len(student_confidence[session_id][student_id])
        logger.info(f"✅ QUALIFIED: {student_id[:8]}... with {appearances} appearances (avg sim: {avg_confidence:.3f})")
        
        # Reset counter for this student to avoid duplicate attendance records
        student_appearances[session_id][student_id] = 0
        return True
    
    return False

# Add this function to cleanup old session data
def cleanup_session_data(session_id: str):
    """Clean up tracking data when session ends"""
    if session_id in student_appearances:
        del student_appearances[session_id]
    if session_id in student_confidence:
        del student_confidence[session_id]
    logger.info(f"🧹 Cleaned up tracking data for session {session_id[:8]}...")

async def is_student_enrolled(session_id: str, student_id: str) -> bool:
    """Check if student is enrolled in the course for this session"""
    try:
        async with httpx.AsyncClient(timeout=3.0) as client:
            response = await client.get(
                f"{SPRING_BOOT_URL}/session/{session_id}/check-student/{student_id}"
            )
            if response.status_code == 200:
                data = response.json()
                return data.get("enrolled", False)
    except Exception as e:
        logger.debug(f"Enrollment check failed: {e}")
    return False

async def check_enrollment_with_cache(session_id: str, student_id: str) -> bool:
    """Check enrollment with caching"""
    cache_key = f"{session_id}:{student_id}"
    
    # Check cache
    if cache_key in enrollment_cache:
        cached_time, enrolled = enrollment_cache[cache_key]
        if time.time() - cached_time < ENROLLMENT_CACHE_TTL:
            return enrolled
    
    # Check enrollment
    enrolled = await is_student_enrolled(session_id, student_id)
    enrollment_cache[cache_key] = (time.time(), enrolled)
    
    if not enrolled:
        logger.info(f"🚫 Student not enrolled in this course - skipping attendance")
    
    return enrolled

# Concentration mapping helper
def convert_concentration_to_float(concentration):
    """Convert concentration from string/dict to float"""
    if isinstance(concentration, (int, float)):
        return float(concentration)
    elif isinstance(concentration, str):
        concentration_map = {
            "low": 0.3,
            "medium": 0.6,
            "high": 0.9,
            "very_low": 0.1,
            "very_high": 0.95
        }
        return concentration_map.get(concentration.lower(), 0.5)
    elif isinstance(concentration, dict):
        level = concentration.get("level", 0.5)
        return convert_concentration_to_float(level)
    else:
        return 0.5

def convert_concentration_to_string(concentration):
    """Convert concentration from float to string (high/medium/low)"""
    float_val = convert_concentration_to_float(concentration)
    if float_val >= 0.7:
        return "high"
    elif float_val >= 0.4:
        return "medium"
    else:
        return "low"

@app.get("/health")
async def health():
    from models.face_recognizer import face_recognizer
    return {
        "status": "ok",
        "enrolled_students": len(face_recognizer._db),
        "service": "EduVision Vision Engine"
    }

@app.post("/reload-embeddings")
async def reload_embeddings():
    """Re-fetch profile pictures from DB and rebuild face embeddings."""
    from models.face_recognizer import face_recognizer
    face_recognizer.reload()
    return {"enrolled": len(face_recognizer._db)}

async def send_attendance_to_spring(session_id: str, student_id: str, student_name: str):
    """Send individual attendance record to Spring Boot"""
    try:
        async with httpx.AsyncClient(timeout=5.0) as client:
            payload = {
                "sessionId": session_id,
                "studentId": student_id,
                "status": "present"
            }
            response = await client.post(
                f"{ATTENDANCE_URL}/record",
                json=payload
            )
            if response.status_code in [200, 201]:
                logger.info(f"📝 Attendance recorded: {student_name}")
            else:
                logger.warning(f"Attendance failed: {response.status_code}")
    except Exception as e:
        logger.debug(f"Attendance endpoint not available: {e}")

def send_to_spring_boot(session_id: str, result):
    """Send detection data to Spring Boot with enrollment check and smart attendance"""
    global student_appearances, student_confidence, enrollment_cache
    
    # Handle both dict and list formats
    if isinstance(result, list):
        # If result is a list, convert to dict format
        people = result
        emotion_counts = {}
        student_count = len(people)
        total_concentration = 0
        
        for person in people:
            emotion = person.get("dominant_emotion", person.get("emotion", "neutral"))
            emotion_counts[emotion] = emotion_counts.get(emotion, 0) + 1
            concentration = convert_concentration_to_float(person.get("concentration", 0.5))
            total_concentration += concentration
        
        avg_concentration = total_concentration / student_count if student_count > 0 else 0.5
        positive_emotions = emotion_counts.get("happy", 0) + emotion_counts.get("surprised", 0)
        total_emotions = sum(emotion_counts.values()) or 1
        engagement_score = (positive_emotions / total_emotions + avg_concentration) / 2
        dominant_emotion = max(emotion_counts, key=emotion_counts.get) if emotion_counts else "neutral"
        
        result_dict = {
            "people": people,
            "student_count": student_count,
            "emotion_counts": emotion_counts,
            "engagement_score": engagement_score,
            "avg_concentration": avg_concentration,
            "dominant_emotion": dominant_emotion
        }
    else:
        # Result is already a dict
        result_dict = result
        people = result_dict.get("people", [])
        emotion_counts = result_dict.get("emotion_counts", {})
        student_count = result_dict.get("student_count", len(people))
    
    # Get dominant emotion
    dominant_emotion = result_dict.get("dominant_emotion", "neutral")
    if not dominant_emotion and emotion_counts:
        dominant_emotion = max(emotion_counts, key=emotion_counts.get)
    
    payload = {
        "sessionId": session_id,
        "seqIndex": int(time.time() * 1000) % 1000000,
        "capturedAt": datetime.now().strftime("%Y-%m-%dT%H:%M:%S"),
        "studentCount": student_count,
        "engagementScore": round(float(result_dict.get("engagement_score", 0.5)), 3),
        "dominantEmotion": dominant_emotion,
        "avgConcentration": round(float(result_dict.get("avg_concentration", 0.5)), 3),
    }
    
    try:
        with httpx.Client(timeout=10.0) as client:
            # Send class snapshot
            response = client.post(
                f"{SPRING_BOOT_URL}/class-snapshot",
                json=payload
            )
            
            if response.status_code in [200, 201]:
                snapshot_data = response.json()
                snapshot_id = snapshot_data.get("snapshotId")
                logger.info(f"✅ Class snapshot sent! Students: {student_count}")
                
                if people and snapshot_id:
                    student_payload = []
                    recognized_names = []
                    
                    for person in people:
                        student_id = person.get("student_id")
                        student_name = person.get("student_name", "Unknown")
                        similarity = person.get("similarity", 0.5)
                        
                        if not student_id:
                            continue
                        
                        # 🔥 Check if student is enrolled in this course using cache
                        cache_key = f"{session_id}:{student_id}"
                        is_enrolled = False
                        
                        # Check cache first
                        if cache_key in enrollment_cache:
                            cached_time, enrolled = enrollment_cache[cache_key]
                            if time.time() - cached_time < ENROLLMENT_CACHE_TTL:
                                is_enrolled = enrolled
                        
                        # If not in cache, check with Spring Boot
                        if not is_enrolled:
                            try:
                                # Use a simple GET request to check enrollment
                                enrollment_response = client.get(
                                    f"{ATTENDANCE_URL}/session/{session_id}/check-student/{student_id}"
                                )
                                if enrollment_response.status_code == 200:
                                    enrollment_data = enrollment_response.json()
                                    is_enrolled = enrollment_data.get("enrolled", False)
                                    # Cache the result
                                    enrollment_cache[cache_key] = (time.time(), is_enrolled)
                            except Exception as e:
                                logger.debug(f"Enrollment check error: {e}")
                                continue  # Skip this student if can't verify enrollment
                        
                        if not is_enrolled:
                            logger.debug(f"⏭️ Skipping {student_name} - not enrolled in this course")
                            continue  # Skip this student entirely
                        
                        recognized_names.append(student_name)
                        
                        # Smart attendance - check threshold
                        if should_mark_attendance(session_id, student_id, similarity):
                            try:
                                attendance_payload = {
                                    "sessionId": session_id,
                                    "studentId": student_id,
                                    "status": "present"
                                }
                                att_response = client.post(
                                    f"{ATTENDANCE_URL}/record",
                                    json=attendance_payload
                                )
                                if att_response.status_code in [200, 201]:
                                    logger.info(f"📝 MARKED PRESENT: {student_name} (threshold met)")
                                else:
                                    logger.warning(f"⚠️ Attendance API error: {att_response.status_code}")
                            except Exception as e:
                                logger.error(f"❌ Attendance API error: {e}")
                        
                        # Get concentration as STRING (for Java DTO)
                        concentration_str = convert_concentration_to_string(person.get("concentration", 0.5))
                        
                        # Get confidence score
                        confidence = person.get("emotion_confidence", person.get("confidence", 0.5))
                        if isinstance(confidence, str):
                            try:
                                confidence = float(confidence)
                            except:
                                confidence = 0.5
                        
                        student_payload.append({
                            "snapshotId": snapshot_id,
                            "sessionId": session_id,
                            "studentId": student_id,
                            "emotion": person.get("dominant_emotion", person.get("emotion", "neutral")),
                            "concentration": concentration_str,
                            "confidenceScore": float(confidence),
                            "capturedAt": datetime.now().strftime("%Y-%m-%dT%H:%M:%S"),
                            "anonymised": False,
                        })
                    
                    if recognized_names:
                        # Show unique recognized students
                        unique = list(set(recognized_names))
                        logger.info(f"👤 Recognized: {', '.join(unique[:5])}" + (f" (+{len(unique)-5})" if len(unique) > 5 else ""))
                    
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
                logger.error(f"❌ Failed to send class snapshot: {response.status_code}")
    except Exception as e:
        logger.error(f"❌ Error sending to Spring Boot: {e}")
        
@app.post("/analyze/frame")
async def analyze_frame(
    session_id: str = Form(...),
    store: bool = Form(True),
    file: UploadFile = File(...)
):
    global current_session_id
    
    try:
        # Read and decode image
        data = await file.read()
        nparr = np.frombuffer(data, np.uint8)
        frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        
        if frame is None:
            return {"error": "Invalid image", "success": False}
        
        # Set session in processor
        processor.current_session_id = session_id
        current_session_id = session_id
        
        # Process frame
        result = processor.process(frame)
        
        # Store in local database
        if store:
            try:
                ensure_session(session_id)
                people_list = result.get("people", [])
                if people_list:
                    insert_detection(session_id, people_list)
            except Exception as e:
                logger.warning(f"Database store error: {e}")
        
        # Send to Spring Boot every 10 seconds
        now = time.time()
        if session_id not in last_flush or (now - last_flush[session_id]) > 10:
            last_flush[session_id] = now
            # Run in background to not block response
            thread = threading.Thread(target=send_to_spring_boot, args=(session_id, result))
            thread.daemon = True
            thread.start()
        
        return {
            "success": True,
            "session_id": session_id,
            "student_count": result.get("student_count", 0),
            "people": result.get("people", [])
        }
        
    except Exception as e:
        logger.error(f"Error processing frame: {e}")
        return {"error": str(e), "success": False}

@app.post("/session/end")
async def end_session(session_id: str = Form(...)):
    """Called when lecture session ends"""
    global current_session_id
    logger.info(f"🏁 Session ended: {session_id}")
    current_session_id = None
    
    # Clear flush tracking for this session
    if session_id in last_flush:
        del last_flush[session_id]
    
    # Cleanup attendance tracking data
    cleanup_session_data(session_id)
    
    # 🔥 Clean up enrollment cache for this session
    keys_to_delete = [k for k in enrollment_cache.keys() if k.startswith(f"{session_id}:")]
    for key in keys_to_delete:
        del enrollment_cache[key]
    
    return {"success": True, "message": f"Session {session_id} ended"}

# Add this function to check if student is enrolled in the course
async def is_student_enrolled(session_id: str, student_id: str) -> bool:
    """Check if student is enrolled in the course for this session"""
    try:
        async with httpx.AsyncClient(timeout=3.0) as client:
            response = await client.get(
                f"{SPRING_BOOT_URL}/session/{session_id}/check-student/{student_id}"
            )
            if response.status_code == 200:
                data = response.json()
                return data.get("enrolled", False)
    except Exception as e:
        logger.debug(f"Enrollment check failed: {e}")
    return False

@app.on_event("startup")
async def startup_event():
    logger.info("🚀 EduVision Vision Engine Started")
    logger.info("   Listening on: http://localhost:8000")
    logger.info("   CORS enabled for: http://localhost:3000")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)