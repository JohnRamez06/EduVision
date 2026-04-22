package com.eduvision.observer;

import com.eduvision.dto.websocket.AlertMessageDTO;
import com.eduvision.model.AlertSeverity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class DashboardNotificationObserver implements SentimentEventObserver {

    private final SimpMessagingTemplate messagingTemplate;

    public DashboardNotificationObserver(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void onEvent(SentimentAlertEvent event) {
        publishAlert(event.getSessionId(), "ENGAGEMENT_DROP", "Class engagement dropped", event.getEngagementScore());
    }

    @EventListener
    public void onSentimentAlert(SentimentAlertEvent event) {
        onEvent(event);
    }

    @EventListener
    public void onStudentAtRisk(StudentAtRiskEvent event) {
        publishAlert(event.getSessionId(), "STUDENT_AT_RISK", "Student flagged as at risk: " + event.getStudentId(), event.getConfidenceScore());
    }

    @EventListener
    public void onConcentrationDrop(ConcentrationDropEvent event) {
        publishAlert(event.getSessionId(), "CONCENTRATION_DROP", "Concentration dropped for student: " + event.getStudentId(), event.getConcentration());
    }

    private void publishAlert(String sessionId, String type, String message, BigDecimal value) {
        if (sessionId == null) {
            return;
        }
        AlertSeverity severity = value != null && value.compareTo(BigDecimal.valueOf(0.30)) < 0
                ? AlertSeverity.critical
                : AlertSeverity.warning;

        String formattedValue = value == null
                ? "n/a"
                : value.multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP) + "%";

        AlertMessageDTO alert = new AlertMessageDTO(type, type.replace('_', ' '), message + " (value: " + formattedValue + ")", severity, sessionId);
        messagingTemplate.convertAndSend("/topic/lecture/" + sessionId + "/alerts", alert);
    }
}
