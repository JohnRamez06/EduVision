package com.eduvision.observer;

import com.eduvision.model.Alert;
import com.eduvision.model.AlertSeverity;
import com.eduvision.model.AlertStatus;
import com.eduvision.model.LectureSession;
import com.eduvision.repository.AlertRepository;
import com.eduvision.repository.SessionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AlertPersistenceObserver implements SentimentEventObserver {

    private final AlertRepository alertRepository;
    private final SessionRepository sessionRepository;

    public AlertPersistenceObserver(AlertRepository alertRepository, SessionRepository sessionRepository) {
        this.alertRepository = alertRepository;
        this.sessionRepository = sessionRepository;
    }

    @Override
    @EventListener
    public void onEvent(SentimentAlertEvent event) {
        persistAlert(event.getSessionId(), event.getSnapshotId(), "Sentiment Alert", event.getMessage(),
                event.getSeverity(), event.getEngagementScore());
    }

    @EventListener
    public void onStudentAtRisk(StudentAtRiskEvent event) {
        persistAlert(event.getSessionId(), event.getSnapshotId(), "Student At Risk",
                "Student " + event.getStudentId() + " marked at risk", AlertSeverity.warning, null);
    }

    @EventListener
    public void onConcentrationDrop(ConcentrationDropEvent event) {
        persistAlert(event.getSessionId(), event.getSnapshotId(), "Concentration Drop",
                String.format("Concentration dropped from %.2f to %.2f",
                        event.getPreviousConcentration(), event.getCurrentConcentration()),
                AlertSeverity.warning, event.getCurrentConcentration());
    }

    private void persistAlert(String sessionId, String snapshotId, String title,
                              String message, AlertSeverity severity, Double actualValue) {
        Optional<LectureSession> sessionOptional = sessionRepository.findById(sessionId);
        if (sessionOptional.isEmpty()) {
            return;
        }

        LectureSession session = sessionOptional.get();
        Alert alert = new Alert();
        alert.setId(UUID.randomUUID().toString());
        alert.setSession(session);
        alert.setCourse(session.getCourse());
        alert.setSeverity(severity);
        alert.setStatus(AlertStatus.open);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setTriggeredAt(LocalDateTime.now());
        if (actualValue != null) {
            alert.setActualValue(BigDecimal.valueOf(actualValue));
        }
        if (snapshotId != null) {
            alert.setAlertMetadata("{\"snapshotId\":\"" + snapshotId + "\"}");
        }
        alertRepository.save(alert);
    }
}
