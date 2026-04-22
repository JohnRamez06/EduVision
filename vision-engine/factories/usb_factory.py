from factories.camera_factory import CameraFactory
from processors.video_stream import VideoStream


class UsbCameraFactory(CameraFactory):
    def __init__(self, device_index: int = 0, width=None, height=None):
        self.device_index = device_index
        self.width = width
        self.height = height

    def create_stream(self):
        return VideoStream(self.device_index, width=self.width, height=self.height)

    def get_config(self) -> dict:
        return {
            "type": "usb",
            "device_index": self.device_index,
            "width": self.width,
            "height": self.height,
        }