from factories.camera_factory import CameraFactory
from processors.video_stream import VideoStream


class VirtualCameraFactory(CameraFactory):
    """
    Virtual camera = video file / prerecorded feed.
    """

    def __init__(self, video_path: str, width=None, height=None):
        self.video_path = video_path
        self.width = width
        self.height = height

    def create_stream(self):
        return VideoStream(self.video_path, width=self.width, height=self.height)

    def get_config(self) -> dict:
        return {
            "type": "virtual",
            "video_path": self.video_path,
            "width": self.width,
            "height": self.height,
        }