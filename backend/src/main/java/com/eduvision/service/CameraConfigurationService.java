package com.eduvision.service;

import com.eduvision.dto.camera.CameraConfigDTO;
import com.eduvision.dto.camera.CameraTestRequest;
import com.eduvision.exception.ResourceNotFoundException;
import com.eduvision.factory.CameraConfigurationFactory;
import com.eduvision.factory.IpCameraFactory;
import com.eduvision.factory.MobileCameraFactory;
import com.eduvision.factory.UsbCameraFactory;
import com.eduvision.model.CameraConfiguration;
import com.eduvision.model.CameraFactoryType;
import com.eduvision.model.User;
import com.eduvision.repository.CameraConfigurationRepository;
import com.eduvision.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CameraConfigurationService {

    private final CameraConfigurationRepository cameraConfigurationRepository;
    private final UserRepository userRepository;
    private final UsbCameraFactory usbCameraFactory;
    private final IpCameraFactory ipCameraFactory;
    private final MobileCameraFactory mobileCameraFactory;

    public CameraConfigurationService(CameraConfigurationRepository cameraConfigurationRepository,
                                      UserRepository userRepository,
                                      UsbCameraFactory usbCameraFactory,
                                      IpCameraFactory ipCameraFactory,
                                      MobileCameraFactory mobileCameraFactory) {
        this.cameraConfigurationRepository = cameraConfigurationRepository;
        this.userRepository = userRepository;
        this.usbCameraFactory = usbCameraFactory;
        this.ipCameraFactory = ipCameraFactory;
        this.mobileCameraFactory = mobileCameraFactory;
    }

    @Transactional(readOnly = true)
    public List<CameraConfigDTO> getConfigs(String userId) {
        if (userId == null || userId.isBlank()) {
            return cameraConfigurationRepository.findAll().stream().map(this::toDto).toList();
        }
        return cameraConfigurationRepository.findByCreatedByIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public CameraConfigDTO createConfig(CameraConfigDTO dto) {
        User createdBy = resolveUser(dto.getCreatedByUserId());
        CameraConfiguration entity = new CameraConfiguration();
        entity.setId(UUID.randomUUID().toString());
        entity.setCreatedBy(createdBy);
        apply(dto, entity);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return toDto(cameraConfigurationRepository.save(entity));
    }

    @Transactional
    public CameraConfigDTO updateConfig(String id, CameraConfigDTO dto) {
        CameraConfiguration entity = cameraConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Camera configuration not found: " + id));
        apply(dto, entity);
        entity.setUpdatedAt(LocalDateTime.now());
        return toDto(cameraConfigurationRepository.save(entity));
    }

    @Transactional
    public void deleteConfig(String id) {
        CameraConfiguration entity = cameraConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Camera configuration not found: " + id));
        cameraConfigurationRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> testConnection(String id, CameraTestRequest request) {
        CameraConfiguration config = cameraConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Camera configuration not found: " + id));
        CameraConfigurationFactory factory = resolveFactory(config.getFactoryType());

        boolean success;
        String message;
        try {
            factory.createStreamReader().connect();
            success = true;
            message = "Connection successful";
        } catch (Exception ex) {
            success = false;
            message = "Connection failed: " + ex.getMessage();
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("cameraId", config.getId());
        response.put("success", success);
        response.put("message", message);
        response.put("timeoutMs", request != null && request.getTimeoutMs() != null ? request.getTimeoutMs() : 3000);
        return response;
    }

    private CameraConfigurationFactory resolveFactory(CameraFactoryType type) {
        if (type == null) {
            return usbCameraFactory;
        }
        return switch (type) {
            case usb -> usbCameraFactory;
            case ip -> ipCameraFactory;
            case mobile -> mobileCameraFactory;
            default -> usbCameraFactory;
        };
    }

    private User resolveUser(String userId) {
        if (userId != null && !userId.isBlank()) {
            return userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        }
        return userRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No users available to own camera configuration"));
    }

    private void apply(CameraConfigDTO source, CameraConfiguration target) {
        target.setName(source.getName());
        target.setFactoryType(source.getFactoryType() != null ? source.getFactoryType() : CameraFactoryType.usb);
        target.setDeviceIndex(source.getDeviceIndex());
        target.setStreamUrl(source.getStreamUrl());
        target.setRtspUsername(source.getRtspUsername());
        target.setRtspPasswordEnc(source.getRtspPassword());
        target.setDeviceToken(source.getDeviceToken());
        target.setResolutionW((short) (source.getResolutionW() != null ? source.getResolutionW() : 1280));
        target.setResolutionH((short) (source.getResolutionH() != null ? source.getResolutionH() : 720));
        target.setFps((byte) (source.getFps() != null ? source.getFps() : 24));
        target.setExtraConfig(source.getExtraConfig());
        target.setActive(source.isActive());
    }

    private CameraConfigDTO toDto(CameraConfiguration entity) {
        CameraConfigDTO dto = new CameraConfigDTO();
        dto.setId(entity.getId());
        dto.setCreatedByUserId(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null);
        dto.setName(entity.getName());
        dto.setFactoryType(entity.getFactoryType());
        dto.setDeviceIndex(entity.getDeviceIndex());
        dto.setStreamUrl(entity.getStreamUrl());
        dto.setRtspUsername(entity.getRtspUsername());
        dto.setRtspPassword(entity.getRtspPasswordEnc());
        dto.setDeviceToken(entity.getDeviceToken());
        dto.setResolutionW((short) entity.getResolutionW());
        dto.setResolutionH((short) entity.getResolutionH());
        dto.setFps((byte) entity.getFps());
        dto.setExtraConfig(entity.getExtraConfig());
        dto.setActive(entity.isActive());
        return dto;
    }
}
