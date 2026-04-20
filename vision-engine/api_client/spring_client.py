import httpx
from config.settings import settings
async def send_snapshot(session_id: str, payload: dict):
    async with httpx.AsyncClient() as client:
        await client.post(f"{settings.spring_backend_url}/api/v1/emotion/snapshot", json=payload)
