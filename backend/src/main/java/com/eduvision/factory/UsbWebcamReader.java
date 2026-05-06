package com.eduvision.factory;

public class UsbWebcamReader implements VideoStreamReader {
    
    private int deviceId;
    private boolean isConnected = false;
    
    public UsbWebcamReader() {
        this.deviceId = 0;
    }
    
    public UsbWebcamReader(int deviceId) {
        this.deviceId = deviceId;
    }
    
    @Override
    public boolean connect() {
        isConnected = true;
        System.out.println("USB Webcam connected - Device ID: " + deviceId);
        return true;
    }
    
    @Override
    public byte[] readFrame() {
        if (!isConnected) return null;
        // This would be handled by Python FastAPI
        return new byte[0];
    }
    
    @Override
    public void disconnect() {
        isConnected = false;
        System.out.println("USB Webcam disconnected");
    }
    
    @Override
    public String getStreamUrl() {
        return "usb://webcam/" + deviceId;
    }
    
    public int getDeviceId() { return deviceId; }
    public void setDeviceId(int deviceId) { this.deviceId = deviceId; }
}