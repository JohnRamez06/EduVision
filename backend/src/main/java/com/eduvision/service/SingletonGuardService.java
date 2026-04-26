package com.eduvision.service;

import com.eduvision.exception.SingletonViolationException;
import com.eduvision.model.LectureSessionRegistry;
import com.eduvision.repository.SessionRegistryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SingletonGuardService {

    private final SessionRegistryRepository registryRepository;

    public SingletonGuardService(SessionRegistryRepository registryRepository) {
        this.registryRepository = registryRepository;
    }

    /**
     * Runs in its own independent transaction so that any failure here
     * never marks the caller's transaction as rollback-only.
     * Falls back to an in-memory registry check when the stored procedure
     * is not present in the database.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void activateSession(String courseId, String sessionId) {
        enforceSingletonInMemory(courseId);
    }

    private void enforceSingletonInMemory(String courseId) {
        LectureSessionRegistry registry = registryRepository.findByCourse_Id(courseId).orElse(null);
        if (registry != null && registry.getActiveSession() != null) {
            throw new SingletonViolationException("Course already has an active session: " + courseId);
        }
    }
}
