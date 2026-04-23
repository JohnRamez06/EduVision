package com.eduvision.repository;

import com.eduvision.model.CourseLecturer;
import com.eduvision.model.CourseLecturerId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseLecturerRepository extends JpaRepository<CourseLecturer, CourseLecturerId> {
    List<CourseLecturer> findByLecturer_User_Id(String lecturerId);
}
