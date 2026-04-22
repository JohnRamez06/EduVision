package com.eduvision.service;

import com.eduvision.dto.websocket.AlertMessageDTO;
import com.eduvision.dto.websocket.MoodUpdateDTO;
import com.eduvision.model.AlertSeverity;
import com.eduvision.singleton.WebSocketSessionManager;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketSessionManager sessionManager;

    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate,
                                        WebSocketSessionManager sessionManager) {
        this.messagingTemplate = messagingTemplate;
        this.sessionManager = sessionManager;
    }

    public void sendAlertToSession(String sessionId, AlertMessageDTO alert) {
        alert.setSessionId(sessionId);
        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/alerts", alert);
    }

    public void sendAlertToLecture(String lectureId, AlertMessageDTO alert) {
        alert.setSessionId(lectureId);
        messagingTemplate.convertAndSend("/topic/lecture/" + lectureId + "/alerts", alert);
    }

    public void broadcastAlertGlobally(AlertMessageDTO alert) {
        messagingTemplate.convertAndSend("/topic/alerts", alert);
    }

    public void sendMoodUpdate(String sessionId, MoodUpdateDTO moodUpdate) {
        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/mood", moodUpdate);
    }

    public void sendToLecturer(String lecturerEmail, AlertMessageDTO alert) {
        messagingTemplate.convertAndSendToUser(lecturerEmail, "/queue/alerts", alert);
    }

    public void sendToUser(String userId, String destination, Object payload) {
        sessionManager.sendToUser(userId, destination, payload);
    }

    public void sendEngagementAlert(String sessionId, double engagementScore, double concentration) {
        AlertSeverity severity = engagementScore < 0.30 ? AlertSeverity.critical
                : engagementScore < 0.55 ? AlertSeverity.warning : AlertSeverity.info;

        AlertMessageDTO alert = new AlertMessageDTO(
                "ENGAGEMENT_DROP",
                "Engagement Drop Detected",
                String.format("Session engagement fell to %.0f%% (concentration: %.0f%%)",
                        engagementScore * 100, concentration * 100),
                severity,
                sessionId);

        sendAlertToSession(sessionId, alert);
        sendAlertToLecture(sessionId, alert);

        if (severity == AlertSeverity.critical) {
            broadcastAlertGlobally(alert);
        }
    }
}
