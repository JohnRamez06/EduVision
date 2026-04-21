package com.eduvision.repository;

import com.eduvision.model.Lecturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LecturerRepository extends JpaRepository<Lecturer, String> {
    boolean existsByEmployeeId(String employeeId);
}
