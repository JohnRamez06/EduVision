package com.eduvision.repository;

import com.eduvision.model.CameraConfiguration;
import com.eduvision.model.CameraFactoryType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CameraConfigurationRepository extends JpaRepository<CameraConfiguration, String> {

    List<CameraConfiguration> findByFactoryType(CameraFactoryType factoryType);

    @Query("select c from CameraConfiguration c where c.active = true")
    List<CameraConfiguration> findActive();
}
