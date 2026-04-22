package com.eduvision.repository;

import com.eduvision.model.SessionCamera;
import com.eduvision.model.SessionCameraId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionCameraRepository extends JpaRepository<SessionCamera, SessionCameraId> {
    List<SessionCamera> findBySession_Id(String sessionId);

    List<SessionCamera> findByCamera_Id(String cameraId);
}
