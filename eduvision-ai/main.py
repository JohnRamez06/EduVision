from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api.routes import emotion, health

app = FastAPI(title="EduVision AI Service", version="1.0.0")
app.add_middleware(CORSMiddleware, allow_origins=["http://localhost:3000","http://localhost:8080"], allow_credentials=True, allow_methods=["*"], allow_headers=["*"])
app.include_router(health.router, prefix="/health", tags=["health"])
app.include_router(emotion.router, prefix="/api/v1/emotion", tags=["emotion"])

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
