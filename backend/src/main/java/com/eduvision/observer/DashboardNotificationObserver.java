package com.eduvision.observer;

import com.eduvision.dto.websocket.AlertMessageDTO;
import com.eduvision.dto.websocket.MoodUpdateDTO;
import com.eduvision.model.AlertSeverity;
import com.eduvision.service.WebSocketNotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DashboardNotificationObserver implements SentimentEventObserver {

    private final WebSocketNotificationService notificationService;

    public DashboardNotificationObserver(WebSocketNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    @EventListener
    public void onEvent(SentimentAlertEvent event) {
        String sessionId = event.getSessionId();
        MoodUpdateDTO moodUpdate = new MoodUpdateDTO(
                sessionId,
                deriveDominantEmotion(event.getEngagementScore()),
                event.getEngagementScore(),
                event.getConcentration(),
                0);

        notificationService.sendMoodUpdate(sessionId, moodUpdate);
        AlertMessageDTO alert = new AlertMessageDTO(
                "ENGAGEMENT_DROP",
                "Sentiment Alert",
                event.getMessage(),
                event.getSeverity(),
                sessionId);
        notificationService.sendAlertToLecture(sessionId, alert);
    }

    @EventListener
    public void onStudentAtRisk(StudentAtRiskEvent event) {
        AlertMessageDTO alert = new AlertMessageDTO(
                "STUDENT_AT_RISK",
                "Student At Risk",
                "Student " + event.getStudentId() + " is at risk based on concentration",
                AlertSeverity.warning,
                event.getSessionId());
        notificationService.sendAlertToLecture(event.getSessionId(), alert);
    }

    @EventListener
    public void onConcentrationDrop(ConcentrationDropEvent event) {
        AlertMessageDTO alert = new AlertMessageDTO(
                "CONCENTRATION_DROP",
                "Concentration Drop",
                String.format("Class concentration dropped from %.2f to %.2f",
                        event.getPreviousConcentration(), event.getCurrentConcentration()),
                AlertSeverity.warning,
                event.getSessionId());
        notificationService.sendAlertToLecture(event.getSessionId(), alert);
    }

    private String deriveDominantEmotion(double engagementScore) {
        if (engagementScore >= 0.75) return "engaged";
        if (engagementScore >= 0.55) return "neutral";
        if (engagementScore >= 0.35) return "confused";
        return "sad";
    }
}
