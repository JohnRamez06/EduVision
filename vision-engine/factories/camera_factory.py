from abc import ABC, abstractmethod
class CameraFactory(ABC):
    @abstractmethod
    def create_capture(self): pass
    @abstractmethod
    def get_config(self) -> dict: pass
