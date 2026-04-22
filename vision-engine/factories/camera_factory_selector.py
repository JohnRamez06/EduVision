from config.settings import settings
from factories.usb_factory import UsbCameraFactory
from factories.ip_factory import IpCameraFactory
from factories.mobile_factory import MobileCameraFactory
from factories.virtual_factory import VirtualCameraFactory


def create_factory_from_settings():
    """
    Uses .env variables if present:
      CAMERA_TYPE=usb|ip|mobile|virtual
      CAMERA_DEVICE_INDEX=0
      CAMERA_RTSP_URL=rtsp://...
      CAMERA_MOBILE_URL=http://...
      CAMERA_VIDEO_PATH=path.mp4
    """
    camera_type = getattr(settings, "camera_type", "usb")

    if camera_type == "usb":
        return UsbCameraFactory(device_index=getattr(settings, "camera_device_index", 0))
    if camera_type == "ip":
        return IpCameraFactory(rtsp_url=getattr(settings, "camera_rtsp_url", ""))
    if camera_type == "mobile":
        return MobileCameraFactory(url=getattr(settings, "camera_mobile_url", ""))
    if camera_type == "virtual":
        return VirtualCameraFactory(video_path=getattr(settings, "camera_video_path", ""))

    raise ValueError(f"Unknown CAMERA_TYPE: {camera_type}")