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
    # spring_client.py — add these functions to your existing SpringClient class

import requests
import logging

logger = logging.getLogger(__name__)

SPRING_BASE_URL = "http://localhost:8080/api/v1"


class SpringClient:
    def __init__(self, auth_token: str):
        self._token = auth_token
        self._session = requests.Session()
        self._session.headers.update({
            "Authorization": f"Bearer {auth_token}",
            "Content-Type": "application/json",
        })

    def send_class_snapshot(self, session_id: str, aggregated_data: dict) -> dict | None:
        """
        POST /api/v1/emotion-data/class-snapshot
        Returns the created snapshot dict (including snapshot_id) or None on failure.
        """
        payload = {
            "sessionId":        session_id,
            "happyCount":       aggregated_data.get("happyCount", 0),
            "neutralCount":     aggregated_data.get("neutralCount", 0),
            "confusedCount":    aggregated_data.get("confusedCount", 0),
            "sadCount":         aggregated_data.get("sadCount", 0),
            "surprisedCount":   aggregated_data.get("surprisedCount", 0),
            "angryCount":       aggregated_data.get("angryCount", 0),
            "avgConcentration": aggregated_data.get("avgConcentration", 0.0),
            "totalFaces":       aggregated_data.get("totalFaces", 0),
            "engagementScore":  aggregated_data.get("engagementScore", 0.0),
        }
        try:
            response = self._session.post(
                f"{SPRING_BASE_URL}/emotion-data/class-snapshot",
                json=payload,
                timeout=10,
            )
            response.raise_for_status()
            data = response.json()
            logger.info(f"Class snapshot sent for session {session_id}, id={data.get('id')}")
            return data
        except requests.RequestException as e:
            logger.error(f"Failed to send class snapshot: {e}")
            return None

    def send_student_snapshots(
        self,
        session_id: str,
        snapshot_id: str | None,
        students_data: list[dict],
    ) -> bool:
        """
        POST /api/v1/emotion-data/student-snapshots?snapshotId={snapshot_id}
        Body: list of per-student dicts.
        Returns True on success.
        """
        if not students_data:
            return True

        payload = [
            {
                "studentId":       s["studentId"],
                "dominantEmotion": s.get("dominantEmotion", "neutral"),
                "concentration":   s.get("concentration", 0.0),
                "confidenceScore": s.get("confidenceScore", 0.0),
                "isDrowsy":        s.get("isDrowsy", False),
                "isAttentive":     s.get("isAttentive", True),
                "sessionId":       session_id,
            }
            for s in students_data
        ]

        params = {}
        if snapshot_id:
            params["snapshotId"] = snapshot_id

        try:
            response = self._session.post(
                f"{SPRING_BASE_URL}/emotion-data/student-snapshots",
                json=payload,
                params=params,
                timeout=10,
            )
            response.raise_for_status()
            logger.info(f"Sent {len(payload)} student snapshots for session {session_id}")
            return True
        except requests.RequestException as e:
            logger.error(f"Failed to send student snapshots: {e}")
            return False