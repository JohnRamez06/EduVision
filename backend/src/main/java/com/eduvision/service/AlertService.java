package com.eduvision.service;

import com.eduvision.dto.alert.AlertDTO;
import com.eduvision.model.Alert;
import com.eduvision.model.AlertSeverity;
import com.eduvision.model.AlertStatus;
import com.eduvision.model.LectureSession;
import com.eduvision.model.Course;
import com.eduvision.repository.AlertRepository;
import com.eduvision.repository.SessionRepository;
import com.eduvision.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AlertService {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private CourseRepository courseRepository;

    public Alert createAlert(String sessionId, String courseId, AlertSeverity severity, String title, String message) {
        Alert alert = new Alert();
        alert.setId(java.util.UUID.randomUUID().toString());

        Optional<LectureSession> session = sessionRepository.findById(sessionId);
        if (session.isPresent()) {
            alert.setSession(session.get());
        }

        Optional<Course> course = courseRepository.findById(courseId);
        if (course.isPresent()) {
            alert.setCourse(course.get());
        }

        alert.setSeverity(severity);
        alert.setStatus(AlertStatus.open);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setTriggeredAt(LocalDateTime.now());

        return alertRepository.save(alert);
    }

    public boolean acknowledgeAlert(String alertId, String userId) {
        Optional<Alert> alertOpt = alertRepository.findById(alertId);
        if (alertOpt.isPresent()) {
            Alert alert = alertOpt.get();
            alert.setStatus(AlertStatus.acknowledged);
            alert.setAcknowledgedAt(LocalDateTime.now());
            // Note: acknowledgedBy would need User repository to set
            alertRepository.save(alert);
            return true;
        }
        return false;
    }

    public boolean resolveAlert(String alertId, String userId) {
        Optional<Alert> alertOpt = alertRepository.findById(alertId);
        if (alertOpt.isPresent()) {
            Alert alert = alertOpt.get();
            alert.setStatus(AlertStatus.resolved);
            alert.setResolvedAt(LocalDateTime.now());
            // Note: resolvedBy would need User repository to set
            alertRepository.save(alert);
            return true;
        }
        return false;
    }

    public List<AlertDTO> getAlertsBySession(String sessionId) {
        List<Alert> alerts = alertRepository.findBySession_Id(sessionId);
        return alerts.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private AlertDTO convertToDTO(Alert alert) {
        return new AlertDTO(
            alert.getId(),
            alert.getSeverity().toString(), // Using severity as type
            alert.getSeverity(),
            alert.getTitle(),
            alert.getMessage(),
            alert.getTriggeredAt(),
            alert.getStatus() != AlertStatus.open
        );
    }
}
