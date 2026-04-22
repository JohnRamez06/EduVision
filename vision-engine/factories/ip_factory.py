from factories.camera_factory import CameraFactory
from processors.video_stream import VideoStream


class IpCameraFactory(CameraFactory):
    def __init__(self, rtsp_url: str, width=None, height=None):
        self.rtsp_url = rtsp_url
        self.width = width
        self.height = height

    def create_stream(self):
        return VideoStream(self.rtsp_url, width=self.width, height=self.height)

    def get_config(self) -> dict:
        return {
            "type": "ip",
            "rtsp_url": self.rtsp_url,
            "width": self.width,
            "height": self.height,
        }