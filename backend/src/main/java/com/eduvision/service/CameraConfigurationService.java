package com.eduvision.service;

import com.eduvision.dto.camera.CameraConfigDTO;
import com.eduvision.exception.ResourceNotFoundException;
import com.eduvision.factory.CameraConfigurationFactory;
import com.eduvision.factory.IpCameraFactory;
import com.eduvision.factory.MobileCameraFactory;
import com.eduvision.factory.UsbCameraFactory;
import com.eduvision.factory.VideoStreamReader;
import com.eduvision.model.CameraConfiguration;
import com.eduvision.model.CameraFactoryType;
import com.eduvision.model.User;
import com.eduvision.repository.CameraConfigurationRepository;
import com.eduvision.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CameraConfigurationService {

    private final CameraConfigurationRepository cameraConfigurationRepository;
    private final UserRepository userRepository;
    private final Map<CameraFactoryType, CameraConfigurationFactory> factories;

    public CameraConfigurationService(CameraConfigurationRepository cameraConfigurationRepository,
                                      UserRepository userRepository,
                                      UsbCameraFactory usbCameraFactory,
                                      IpCameraFactory ipCameraFactory,
                                      MobileCameraFactory mobileCameraFactory) {
        this.cameraConfigurationRepository = cameraConfigurationRepository;
        this.userRepository = userRepository;
        this.factories = new EnumMap<>(CameraFactoryType.class);
        this.factories.put(CameraFactoryType.usb, usbCameraFactory);
        this.factories.put(CameraFactoryType.ip, ipCameraFactory);
        this.factories.put(CameraFactoryType.mobile, mobileCameraFactory);
    }

    @Transactional(readOnly = true)
    public List<CameraConfigDTO> getConfigs(boolean activeOnly) {
        List<CameraConfiguration> configs = activeOnly
                ? cameraConfigurationRepository.findActive()
                : cameraConfigurationRepository.findAll();
        return configs.stream().map(this::toDto).toList();
    }

    @Transactional
    public CameraConfigDTO createConfig(CameraConfigDTO dto) {
        CameraConfiguration config = new CameraConfiguration();
        config.setId(UUID.randomUUID().toString());
        mapToEntity(dto, config, true);
        return toDto(cameraConfigurationRepository.save(config));
    }

    @Transactional
    public CameraConfigDTO updateConfig(String id, CameraConfigDTO dto) {
        CameraConfiguration config = cameraConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Camera config not found: " + id));
        mapToEntity(dto, config, false);
        return toDto(cameraConfigurationRepository.save(config));
    }

    @Transactional
    public void deleteConfig(String id) {
        cameraConfigurationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> testConnection(String id, Integer timeoutMs) {
        CameraConfiguration config = cameraConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Camera config not found: " + id));

        CameraConfigurationFactory factory = factories.get(config.getFactoryType());
        if (factory == null) {
            return Map.of("success", false, "message", "Unsupported camera factory: " + config.getFactoryType());
        }

        VideoStreamReader reader = factory.createStreamReader();
        try {
            reader.connect();
            byte[] frame = reader.readFrame();
            return Map.of(
                    "success", frame != null,
                    "timeoutMs", timeoutMs,
                    "message", frame != null ? "Camera connection successful" : "No frame received");
        } catch (Exception exception) {
            return Map.of("success", false, "message", exception.getMessage());
        } finally {
            try {
                reader.disconnect();
            } catch (Exception ignored) {
            }
        }
    }

    private void mapToEntity(CameraConfigDTO dto, CameraConfiguration entity, boolean isCreate) {
        entity.setName(dto.getName());
        entity.setFactoryType(dto.getFactoryType());
        entity.setDeviceIndex(dto.getDeviceIndex());
        entity.setStreamUrl(dto.getStreamUrl());
        entity.setRtspUsername(dto.getRtspUsername());
        entity.setRtspPasswordEnc(dto.getRtspPasswordEnc());
        entity.setDeviceToken(dto.getDeviceToken());
        entity.setResolutionW(dto.getResolutionW());
        entity.setResolutionH(dto.getResolutionH());
        entity.setFps(dto.getFps());
        entity.setExtraConfig(dto.getExtraConfig());
        entity.setActive(dto.isActive());
        entity.setUpdatedAt(LocalDateTime.now());

        if (isCreate) {
            entity.setCreatedAt(LocalDateTime.now());
            String createdById = dto.getCreatedById();
            if (createdById == null) {
                throw new IllegalArgumentException("createdById is required");
            }
            User createdBy = userRepository.findById(createdById)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + createdById));
            entity.setCreatedBy(createdBy);
        }
    }

    private CameraConfigDTO toDto(CameraConfiguration config) {
        CameraConfigDTO dto = new CameraConfigDTO();
        dto.setId(config.getId());
        dto.setName(config.getName());
        dto.setFactoryType(config.getFactoryType());
        dto.setDeviceIndex(config.getDeviceIndex());
        dto.setStreamUrl(config.getStreamUrl());
        dto.setRtspUsername(config.getRtspUsername());
        dto.setRtspPasswordEnc(config.getRtspPasswordEnc());
        dto.setDeviceToken(config.getDeviceToken());
        dto.setResolutionW(config.getResolutionW());
        dto.setResolutionH(config.getResolutionH());
        dto.setFps(config.getFps());
        dto.setExtraConfig(config.getExtraConfig());
        dto.setActive(config.isActive());
        dto.setCreatedById(config.getCreatedBy() == null ? null : config.getCreatedBy().getId());
        return dto;
    }
}
