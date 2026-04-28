import httpx
from config.settings import settings

def send_snapshot(session_id: str, payload: dict):
    """
    Sends a class snapshot to Spring Boot so it gets stored in emotion_snapshots.
    Endpoint is expected to exist in backend.
    """
    url = f"{settings.spring_backend_url}/api/v1/emotion/snapshot"
    payload = dict(payload)
    payload["sessionId"] = session_id  # ensure Spring gets real lecture_sessions.id

    with httpx.Client(timeout=10.0) as client:
        r = client.post(url, json=payload)
        r.raise_for_status()
        return r.json() if r.content else None