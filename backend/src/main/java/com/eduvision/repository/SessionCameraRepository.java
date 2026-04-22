package com.eduvision.repository;

import com.eduvision.model.SessionCamera;
import com.eduvision.model.SessionCameraId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionCameraRepository extends JpaRepository<SessionCamera, SessionCameraId> {

    @Query("select sc from SessionCamera sc where sc.session.id = :sessionId")
    List<SessionCamera> findBySessionId(@Param("sessionId") String sessionId);
}
