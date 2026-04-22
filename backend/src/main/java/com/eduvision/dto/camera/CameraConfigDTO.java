package com.eduvision.dto.camera;

import com.eduvision.model.CameraFactoryType;

public class CameraConfigDTO {
    private String id;
    private String createdByUserId;
    private String name;
    private CameraFactoryType factoryType;
    private Short deviceIndex;
    private String streamUrl;
    private String rtspUsername;
    private String rtspPassword;
    private String deviceToken;
    private Short resolutionW;
    private Short resolutionH;
    private Byte fps;
    private String extraConfig;
    private boolean active;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(String createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CameraFactoryType getFactoryType() {
        return factoryType;
    }

    public void setFactoryType(CameraFactoryType factoryType) {
        this.factoryType = factoryType;
    }

    public Short getDeviceIndex() {
        return deviceIndex;
    }

    public void setDeviceIndex(Short deviceIndex) {
        this.deviceIndex = deviceIndex;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }

    public String getRtspUsername() {
        return rtspUsername;
    }

    public void setRtspUsername(String rtspUsername) {
        this.rtspUsername = rtspUsername;
    }

    public String getRtspPassword() {
        return rtspPassword;
    }

    public void setRtspPassword(String rtspPassword) {
        this.rtspPassword = rtspPassword;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public Short getResolutionW() {
        return resolutionW;
    }

    public void setResolutionW(Short resolutionW) {
        this.resolutionW = resolutionW;
    }

    public Short getResolutionH() {
        return resolutionH;
    }

    public void setResolutionH(Short resolutionH) {
        this.resolutionH = resolutionH;
    }

    public Byte getFps() {
        return fps;
    }

    public void setFps(Byte fps) {
        this.fps = fps;
    }

    public String getExtraConfig() {
        return extraConfig;
    }

    public void setExtraConfig(String extraConfig) {
        this.extraConfig = extraConfig;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
