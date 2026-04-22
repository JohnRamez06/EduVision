package com.eduvision.dto.camera;

import com.eduvision.model.CameraFactoryType;

public class CameraConfigDTO {

    private String id;
    private String name;
    private CameraFactoryType factoryType;
    private Short deviceIndex;
    private String streamUrl;
    private String rtspUsername;
    private String rtspPasswordEnc;
    private String deviceToken;
    private short resolutionW;
    private short resolutionH;
    private byte fps;
    private String extraConfig;
    private boolean active;
    private String createdById;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public CameraFactoryType getFactoryType() { return factoryType; }
    public void setFactoryType(CameraFactoryType factoryType) { this.factoryType = factoryType; }
    public Short getDeviceIndex() { return deviceIndex; }
    public void setDeviceIndex(Short deviceIndex) { this.deviceIndex = deviceIndex; }
    public String getStreamUrl() { return streamUrl; }
    public void setStreamUrl(String streamUrl) { this.streamUrl = streamUrl; }
    public String getRtspUsername() { return rtspUsername; }
    public void setRtspUsername(String rtspUsername) { this.rtspUsername = rtspUsername; }
    public String getRtspPasswordEnc() { return rtspPasswordEnc; }
    public void setRtspPasswordEnc(String rtspPasswordEnc) { this.rtspPasswordEnc = rtspPasswordEnc; }
    public String getDeviceToken() { return deviceToken; }
    public void setDeviceToken(String deviceToken) { this.deviceToken = deviceToken; }
    public short getResolutionW() { return resolutionW; }
    public void setResolutionW(short resolutionW) { this.resolutionW = resolutionW; }
    public short getResolutionH() { return resolutionH; }
    public void setResolutionH(short resolutionH) { this.resolutionH = resolutionH; }
    public byte getFps() { return fps; }
    public void setFps(byte fps) { this.fps = fps; }
    public String getExtraConfig() { return extraConfig; }
    public void setExtraConfig(String extraConfig) { this.extraConfig = extraConfig; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getCreatedById() { return createdById; }
    public void setCreatedById(String createdById) { this.createdById = createdById; }
}
