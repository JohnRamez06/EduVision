from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    db_host: str = "localhost"
    db_port: int = 3306
    db_name: str = "eduvision"
    db_user: str = "root"
    db_password: str = ""
    spring_backend_url: str = "http://localhost:8080"
    camera_device_index: int = 0

    class Config:
        env_file = ".env"

settings = Settings()