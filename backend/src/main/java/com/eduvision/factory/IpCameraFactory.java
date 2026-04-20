package com.eduvision.factory;
import org.springframework.stereotype.Component;
@Component
public class IpCameraFactory implements CameraConfigurationFactory {
    @Override public VideoStreamReader createStreamReader() { return new UsbWebcamReader(); }
    @Override public FaceDetectionAlgorithm createFaceDetector() { return new HaarCascadeDetector(); }
}
