package com.eduvision.factory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CameraConfigurationFactoryTest {
    @Test void usbFactoryCreatesCorrectProducts() {
        CameraConfigurationFactory factory = new UsbCameraFactory();
        assertNotNull(factory.createStreamReader());
        assertNotNull(factory.createFaceDetector());
    }
}
