package com.eduvision.repository;

import com.eduvision.model.LectureSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<LectureSession, String> {
}
