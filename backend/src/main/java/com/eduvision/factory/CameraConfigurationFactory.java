package com.eduvision.factory;

import org.springframework.stereotype.Component;

@Component
public class CameraConfigurationFactory {
    
    public CameraConfiguration createCamera(String cameraType, String cameraId) {
        CameraConfiguration config = new CameraConfiguration();
        config.setType(cameraType);
        config.setCameraId(cameraId);
        
        switch (cameraType.toLowerCase()) {
            case "usb":
                int deviceId = Integer.parseInt(cameraId);
                config.setReader(new UsbWebcamReader(deviceId));
                break;
            case "builtin":
                config.setReader(new UsbWebcamReader(0));
                break;
            default:
                config.setReader(new UsbWebcamReader(0));
        }
        
        return config;
    }
    
    public static class CameraConfiguration {
        private String type;
        private String cameraId;
        private VideoStreamReader reader;
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getCameraId() { return cameraId; }
        public void setCameraId(String cameraId) { this.cameraId = cameraId; }
        public VideoStreamReader getReader() { return reader; }
        public void setReader(VideoStreamReader reader) { this.reader = reader; }
    }
}