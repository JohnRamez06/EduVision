from abc import ABC, abstractmethod
from typing import Any, Dict


class CameraFactory(ABC):
    @abstractmethod
    def create_stream(self):
        """Return a VideoStream instance."""
        raise NotImplementedError

    @abstractmethod
    def get_config(self) -> Dict[str, Any]:
        raise NotImplementedError