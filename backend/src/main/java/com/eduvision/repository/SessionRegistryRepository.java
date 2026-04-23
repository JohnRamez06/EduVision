package com.eduvision.repository;

import com.eduvision.model.LectureSessionRegistry;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRegistryRepository extends JpaRepository<LectureSessionRegistry, String> {
    Optional<LectureSessionRegistry> findByCourse_Id(String courseId);
}
