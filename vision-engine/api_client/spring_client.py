import httpx
from config.settings import settings
import uuid
from datetime import datetime

def send_snapshot(session_id: str, payload: dict):
    """Sends class snapshot to Spring Boot"""
    url = f"{settings.spring_backend_url}/api/v1/emotion-data/class-snapshot"
    payload = dict(payload)
    payload["sessionId"] = session_id
    payload["snapshotId"] = payload.get("snapshotId", str(uuid.uuid4()))
    payload["seqIndex"] = payload.get("seqIndex", 0)
    payload["capturedAt"] = datetime.now().isoformat()

    with httpx.Client(timeout=10.0) as client:
        r = client.post(url, json=payload)
        r.raise_for_status()
        return r.json() if r.content else None


def send_student_snapshots(session_id: str, snapshot_id: str, students: list):
    """Sends per-student emotion + attendance data to Spring Boot"""
    url = f"{settings.spring_backend_url}/api/v1/emotion-data/student-snapshots?snapshotId={snapshot_id}"
    
    payload = []
    for student in students:
        payload.append({
            "studentId": student.get("studentId"),
            "sessionId": session_id,
            "snapshotId": snapshot_id,
            "emotion": student.get("dominant_emotion", "neutral"),
            "concentration": student.get("concentration", {}).get("level", "medium"),
            "confidenceScore": student.get("confidence", 0.0),
            "capturedAt": datetime.now().isoformat(),
            "isAnonymised": False
        })
    
    with httpx.Client(timeout=10.0) as client:
        r = client.post(url, json=payload)
        r.raise_for_status()
        return r.json() if r.content else None