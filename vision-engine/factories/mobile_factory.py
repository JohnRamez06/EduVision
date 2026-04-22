from factories.camera_factory import CameraFactory
from processors.video_stream import VideoStream


class MobileCameraFactory(CameraFactory):
    """
    Mobile camera usually comes via DroidCam/IVCam/MJPEG URL.
    Example: http://192.168.0.10:4747/video
    """

    def __init__(self, url: str, width=None, height=None):
        self.url = url
        self.width = width
        self.height = height

    def create_stream(self):
        return VideoStream(self.url, width=self.width, height=self.height)

    def get_config(self) -> dict:
        return {
            "type": "mobile",
            "url": self.url,
            "width": self.width,
            "height": self.height,
        }