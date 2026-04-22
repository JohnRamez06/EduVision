package com.eduvision.repository;

import com.eduvision.model.Report;
import com.eduvision.model.ReportStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, String> {
    List<Report> findByRequestedByIdOrderByRequestedAtDesc(String requestedById);

    List<Report> findByRequestedByIdAndStatusOrderByRequestedAtDesc(String requestedById, ReportStatus status);

    Optional<Report> findByIdAndRequestedById(String id, String requestedById);
}
