package com.eduvision.repository;

import com.eduvision.model.CameraConfiguration;
import com.eduvision.model.CameraFactoryType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CameraConfigurationRepository extends JpaRepository<CameraConfiguration, String> {
    List<CameraConfiguration> findByCreatedByIdOrderByCreatedAtDesc(String createdById);

    List<CameraConfiguration> findByFactoryTypeAndActiveTrue(CameraFactoryType factoryType);

    Optional<CameraConfiguration> findByIdAndCreatedById(String id, String createdById);
}
