package com.eduvision.service;

import com.eduvision.exception.SingletonViolationException;
import com.eduvision.model.LectureSessionRegistry;
import com.eduvision.repository.SessionRegistryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.persistence.PersistenceException;
import org.springframework.stereotype.Service;

@Service
public class SingletonGuardService {

    private final SessionRegistryRepository registryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public SingletonGuardService(SessionRegistryRepository registryRepository) {
        this.registryRepository = registryRepository;
    }

    public void activateSession(String courseId, String sessionId) {
        try {
            StoredProcedureQuery procedure = entityManager.createStoredProcedureQuery("sp_activate_session");
            procedure.registerStoredProcedureParameter("courseId", String.class, ParameterMode.IN);
            procedure.registerStoredProcedureParameter("sessionId", String.class, ParameterMode.IN);
            procedure.setParameter("courseId", courseId);
            procedure.setParameter("sessionId", sessionId);
            procedure.execute();
            return;
        } catch (IllegalArgumentException | PersistenceException ex) {
            enforceSingletonInMemory(courseId);
        }
    }

    private void enforceSingletonInMemory(String courseId) {
        LectureSessionRegistry registry = registryRepository.findByCourse_Id(courseId).orElse(null);
        if (registry != null && registry.getActiveSession() != null) {
            throw new SingletonViolationException("Course already has an active session: " + courseId);
        }
    }
}
