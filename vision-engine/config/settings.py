from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    db_host: str = "localhost"
    db_port: int = 3306
    db_name: str = "eduvision"
    db_user: str = "root"
    db_password: str = ""
    # Add these to your settings
    spring_backend_url: str = "http://localhost:8080"
    active_session_id: str | None = None
    store_to_spring: bool = True
    store_to_minimal_db: bool = False
    camera_device_index: int = 0

    class Config:
        env_file = ".env"

settings = Settings()