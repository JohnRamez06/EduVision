package com.eduvision.factory;
public interface CameraConfigurationFactory {
    VideoStreamReader createStreamReader();
    FaceDetectionAlgorithm createFaceDetector();
}
