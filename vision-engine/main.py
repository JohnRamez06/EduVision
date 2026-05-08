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

from database.schema import ensure_schema
from database.repository import ensure_session, insert_detection
from processors.frame_processor import FrameProcessor

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Spring Boot URL
SPRING_BOOT_URL = "http://localhost:8080/api/v1/emotion-data"
ATTENDANCE_URL = "http://localhost:8080/api/v1/attendance"
ALERTS_URL = "http://localhost:8080/api/v1/alerts"

app = FastAPI(title="EduVision Vision Engine", version="1.0.0")

# Global tracking variables
student_appearances = defaultdict(lambda: defaultdict(int))
student_confidence = defaultdict(lambda: defaultdict(list))
ATTENDANCE_THRESHOLD = 3
SIMILARITY_THRESHOLD = 0.6
enrollment_cache = {}
ENROLLMENT_CACHE_TTL = 300

# CORS Configuration
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

# Create processor and give it an HTTP client so it can send leave events
processor = FrameProcessor()
processor.spring_client = httpx.Client(timeout=5.0)

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
    """Determine if a student should be marked as present"""
    global student_appearances, student_confidence
    if similarity < SIMILARITY_THRESHOLD:
        logger.debug(f"⏭️ Skipping {student_id[:8]}... similarity too low: {similarity:.3f}")
        return False
    student_appearances[session_id][student_id] += 1
    student_confidence[session_id][student_id].append(similarity)
    appearances = student_appearances[session_id][student_id]
    logger.debug(f"📊 {student_id[:8]}... appearances: {appearances}/{ATTENDANCE_THRESHOLD}")
    if appearances >= ATTENDANCE_THRESHOLD:
        avg_confidence = sum(student_confidence[session_id][student_id]) / len(student_confidence[session_id][student_id])
        logger.info(f"✅ QUALIFIED: {student_id[:8]}... with {appearances} appearances (avg sim: {avg_confidence:.3f})")
        student_appearances[session_id][student_id] = 0
        return True
    return False

def cleanup_session_data(session_id: str):
    if session_id in student_appearances:
        del student_appearances[session_id]
    if session_id in student_confidence:
        del student_confidence[session_id]
    logger.info(f"🧹 Cleaned up tracking data for session {session_id[:8]}...")

def convert_concentration_to_float(concentration):
    if isinstance(concentration, (int, float)):
        return float(concentration)
    elif isinstance(concentration, str):
        concentration_map = {"low": 0.3, "medium": 0.6, "high": 0.9, "very_low": 0.1, "very_high": 0.95}
        return concentration_map.get(concentration.lower(), 0.5)
    elif isinstance(concentration, dict):
        level = concentration.get("level", 0.5)
        return convert_concentration_to_float(level)
    return 0.5

def convert_concentration_to_string(concentration):
    float_val = convert_concentration_to_float(concentration)
    if float_val >= 0.7:
        return "high"
    elif float_val >= 0.4:
        return "medium"
    return "low"

def check_and_send_alerts(session_id: str, emotion_counts: dict, total_students: int, engagement_score: float, client: httpx.Client):
    logger.info(f"🔍 Checking alerts: total_students={total_students}, emotions={emotion_counts}, engagement={engagement_score:.2f}")
    confused_count = emotion_counts.get('confused', 0)
    if total_students > 0:
        confusion_ratio = confused_count / total_students
        if confusion_ratio > 0.01:
            alert_payload = {
                "sessionId": session_id,
                "title": "⚠️ High Confusion Detected",
                "message": f"{confused_count} out of {total_students} students appear confused ({int(confusion_ratio*100)}%)",
                "severity": "critical",
                "alertType": "confusion_warning"
            }
            try:
                response = client.post(f"{ALERTS_URL}", json=alert_payload)
                if response.status_code in [200, 201]:
                    logger.warning(f"🚨 ALERT SENT: High Confusion Detected")
            except Exception as e:
                logger.error(f"Error sending confusion alert: {e}")
    if total_students > 0 and engagement_score < 0.5:
        alert_payload = {
            "sessionId": session_id,
            "title": "⚠️ Very Low Engagement",
            "message": f"Class engagement is only {int(engagement_score*100)}%. Consider interactive activities.",
            "severity": "warning",
            "alertType": "engagement_warning"
        }
        try:
            response = client.post(f"{ALERTS_URL}", json=alert_payload)
            if response.status_code in [200, 201]:
                logger.warning(f"🚨 ALERT SENT: Very Low Engagement")
        except Exception as e:
            logger.error(f"Error sending engagement alert: {e}")

def record_attendance_with_presence(client: httpx.Client, session_id: str, student_id: str, student_name: str, presence_event: str):
    try:
        attendance_payload = {
            "sessionId": session_id,
            "studentId": student_id,
            "status": "present",
            "presenceEvent": presence_event if presence_event else "present"
        }
        logger.info(f"📤 Sending presence: {student_name} -> event: {presence_event}")
        response = client.post(
            f"{ATTENDANCE_URL}/record-with-presence",
            json=attendance_payload,
            timeout=5.0
        )
        if response.status_code in [200, 201]:
            if presence_event == "joined":
                logger.info(f"✅ JOINED: {student_name}")
            elif presence_event == "returned":
                logger.info(f"🔄 RETURNED: {student_name}")
                # Also close the open exit log so return_time gets set
                try:
                    ret_resp = client.post(
                        f"{ATTENDANCE_URL}/return",
                        json={"sessionId": session_id, "studentId": student_id},
                        timeout=5.0
                    )
                    if ret_resp.status_code in [200, 201]:
                        logger.info(f"✅ Exit log closed for {student_name}")
                    else:
                        logger.warning(f"Exit log close failed: {ret_resp.status_code}")
                except Exception as re:
                    logger.warning(f"Could not close exit log: {re}")
            elif presence_event == "left":
                logger.info(f"🚪 LEFT: {student_name}")
            else:
                logger.info(f"📝 Present: {student_name}")
            return True
        else:
            logger.warning(f"Attendance failed: {response.status_code}")
            return False
    except Exception as e:
        logger.error(f"Attendance error: {e}")
        return False

def send_to_spring_boot(session_id: str, result):
    """Send detection data to Spring Boot with enrollment check and smart attendance"""
    global student_appearances, student_confidence, enrollment_cache

    # Handle both dict and list formats
    if isinstance(result, list):
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
        result_dict = result
        people = result_dict.get("people", [])
        emotion_counts = result_dict.get("emotion_counts", {})
        student_count = result_dict.get("student_count", len(people))
        engagement_score = result_dict.get("engagement_score", 0.5)
        avg_concentration = result_dict.get("avg_concentration", 0.5)
        dominant_emotion = result_dict.get("dominant_emotion", "neutral")
        if not dominant_emotion and emotion_counts:
            dominant_emotion = max(emotion_counts, key=emotion_counts.get)

    # 🔥 FIX: ensure no None values slip through
    if emotion_counts is None:
        emotion_counts = {}
    if engagement_score is None:
        engagement_score = 0.5
    if avg_concentration is None:
        avg_concentration = 0.5
    if dominant_emotion is None:
        dominant_emotion = "neutral"

    payload = {
        "sessionId": session_id,
        "seqIndex": int(time.time() * 1000) % 1000000,
        "capturedAt": datetime.now().strftime("%Y-%m-%dT%H:%M:%S"),
        "studentCount": student_count,
        "engagementScore": round(float(engagement_score), 3),
        "dominantEmotion": dominant_emotion,
        "avgConcentration": round(float(avg_concentration), 3),
    }

    try:
        with httpx.Client(timeout=10.0) as client:
            response = client.post(f"{SPRING_BOOT_URL}/class-snapshot", json=payload)
            if response.status_code in [200, 201]:
                snapshot_data = response.json()
                snapshot_id = snapshot_data.get("snapshotId")
                logger.info(f"✅ Class snapshot sent! Students: {student_count}")
                
                if student_count > 0:
                    check_and_send_alerts(session_id, emotion_counts, student_count, engagement_score, client)
                
                if people and snapshot_id:
                    student_payload = []
                    recognized_names = []
                    for person in people:
                        student_id = person.get("student_id")
                        student_name = person.get("student_name", "Unknown")
                        similarity = person.get("similarity", 0.5)
                        presence_event = person.get("presence_event")
                        if not student_id:
                            continue
                        # enrollment check
                        is_enrolled = check_enrollment_sync(session_id, student_id)
                        if not is_enrolled:
                            logger.warning(f"🚫 UNWANTED STUDENT: {student_name} NOT enrolled - SKIPPING attendance")
                            recognized_names.append(student_name)
                            continue
                        recognized_names.append(student_name)
                        record_attendance_with_presence(client, session_id, student_id, student_name, presence_event)
                        concentration_str = convert_concentration_to_string(person.get("concentration", 0.5))
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
                        unique = list(set(recognized_names))
                        logger.info(f"👤 Recognized: {', '.join(unique[:5])}")
                    if student_payload:
                        student_response = client.post(
                            f"{SPRING_BOOT_URL}/student-snapshots?snapshotId={snapshot_id}",
                            json=student_payload
                        )
                        if student_response.status_code in [200, 201]:
                            logger.info(f"✅ Student snapshots sent ({len(student_payload)} students)")
            else:
                logger.error(f"❌ Failed to send class snapshot: {response.status_code}")
    except Exception as e:
        logger.error(f"❌ Error sending to Spring Boot: {e}")

        
def check_enrollment_sync(session_id: str, student_id: str) -> bool:
    cache_key = f"{session_id}:{student_id}"
    logger.info(f"🔍 ENROLLMENT CHECK (sync): student={student_id}, session={session_id}")
    if cache_key in enrollment_cache:
        cached_time, enrolled = enrollment_cache[cache_key]
        if time.time() - cached_time < ENROLLMENT_CACHE_TTL:
            logger.info(f"📦 CACHE HIT: enrolled={enrolled}")
            return enrolled
    try:
        with httpx.Client(timeout=3.0) as client:
            response = client.get(
                f"{ATTENDANCE_URL}/session/{session_id}/check-student/{student_id}"
            )
            if response.status_code == 200:
                data = response.json()
                enrolled = data.get("enrolled", False)
            else:
                logger.warning(f"Enrollment API returned {response.status_code}")
                enrolled = False
    except Exception as e:
        logger.error(f"Enrollment check error: {e}")
        enrolled = False
    enrollment_cache[cache_key] = (time.time(), enrolled)
    logger.info(f"📊 ENROLLMENT RESULT: {enrolled}")
    if not enrolled:
        logger.warning(f"🚫 UNWANTED STUDENT: Student {student_id} NOT enrolled with this lecturer!")
    return enrolled

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
    from models.face_recognizer import face_recognizer
    face_recognizer.reload()
    return {"enrolled": len(face_recognizer._db)}

@app.post("/analyze/frame")
async def analyze_frame(
    session_id: str = Form(...),
    store: bool = Form(True),
    file: UploadFile = File(...)
):
    global current_session_id
    try:
        data = await file.read()
        nparr = np.frombuffer(data, np.uint8)
        frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        if frame is None:
            return {"error": "Invalid image", "success": False}
        processor.current_session_id = session_id
        current_session_id = session_id
        result = processor.process(frame)
        if store:
            try:
                ensure_session(session_id)
                people_list = result.get("people", [])
                if people_list:
                    insert_detection(session_id, people_list)
            except Exception as e:
                logger.warning(f"Database store error: {e}")
        now = time.time()
        if session_id not in last_flush or (now - last_flush[session_id]) > 10:
            last_flush[session_id] = now
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
    global current_session_id
    logger.info(f"🏁 Session ended: {session_id}")
    current_session_id = None
    if session_id in last_flush:
        del last_flush[session_id]
    cleanup_session_data(session_id)
    processor.clear_session_presence()
    keys_to_delete = [k for k in enrollment_cache.keys() if k.startswith(f"{session_id}:")]
    for key in keys_to_delete:
        del enrollment_cache[key]
    return {"success": True, "message": f"Session {session_id} ended"}

async def is_student_enrolled(session_id: str, student_id: str) -> bool:
    try:
        async with httpx.AsyncClient(timeout=3.0) as client:
            response = await client.get(
                f"{ATTENDANCE_URL}/session/{session_id}/check-student/{student_id}"
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