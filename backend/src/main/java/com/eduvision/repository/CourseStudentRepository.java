package com.eduvision.repository;

import com.eduvision.model.CourseStudent;
import com.eduvision.model.CourseStudentId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseStudentRepository extends JpaRepository<CourseStudent, CourseStudentId> {

    List<CourseStudent> findByStudent_IdAndDroppedAtIsNull(String studentId);
}
