package com.eduvision.service;

import com.eduvision.dto.websocket.AlertMessageDTO;
import com.eduvision.dto.websocket.MoodUpdateDTO;
import com.eduvision.model.AlertSeverity;
import com.eduvision.singleton.WebSocketSessionManager;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Central service for pushing real-time messages to the React dashboard.
 *
 * Topic layout (mirrors what React subscribes to):
 *   /topic/session/{sessionId}/alerts   — broadcast alert to all watchers of a session
 *   /topic/session/{sessionId}/mood     — live mood/concentration update for a session
 *   /topic/alerts                       — global alert feed (admin / dean view)
 *   /user/queue/alerts                  — private alert for a specific lecturer/admin
 */
@Service
public class WebSocketNotificationService {

    private final SimpMessagingTemplate    messagingTemplate;
    private final WebSocketSessionManager  sessionManager;

    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate,
                                         WebSocketSessionManager sessionManager) {
        this.messagingTemplate = messagingTemplate;
        this.sessionManager    = sessionManager;
    }

    // ─── ALERT BROADCASTING ───────────────────────────────────────────────

    /**
     * Pushes an alert to EVERY client watching a specific lecture session.
     * React subscribes to: /topic/session/{sessionId}/alerts
     */
    public void sendAlertToSession(String sessionId, AlertMessageDTO alert) {
        alert.setSessionId(sessionId);
        messagingTemplate.convertAndSend(
                "/topic/session/" + sessionId + "/alerts", alert);
    }

    /**
     * Pushes the same alert to the global feed (admin/dean dashboard).
     * React subscribes to: /topic/alerts
     */
    public void broadcastAlertGlobally(AlertMessageDTO alert) {
        messagingTemplate.convertAndSend("/topic/alerts", alert);
    }

    // ─── MOOD / CONCENTRATION UPDATES ────────────────────────────────────

    /**
     * Pushes a real-time mood snapshot to all clients watching this session.
     * React subscribes to: /topic/session/{sessionId}/mood
     *
     * Called by DashboardNotificationObserver on every SentimentAlertEvent.
     */
    public void sendMoodUpdate(String sessionId, MoodUpdateDTO moodUpdate) {
        messagingTemplate.convertAndSend(
                "/topic/session/" + sessionId + "/mood", moodUpdate);
    }

    // ─── PRIVATE / TARGETED MESSAGES ─────────────────────────────────────

    /**
     * Sends a private alert to a specific lecturer's queue.
     * Uses Spring's user-destination prefix so only that user receives it.
     * React subscribes to: /user/queue/alerts  (Spring resolves per-user)
     *
     * @param lecturerEmail the JWT subject (email) of the target lecturer
     */
    public void sendToLecturer(String lecturerEmail, AlertMessageDTO alert) {
        messagingTemplate.convertAndSendToUser(
                lecturerEmail, "/queue/alerts", alert);
    }

    /**
     * Sends a private notification to any user by their email/JWT subject.
     *
     * @param userId      JWT subject (email) of the target user
     * @param destination e.g. "alerts", "notifications"
     */
    public void sendToUser(String userId, String destination, Object payload) {
        sessionManager.sendToUser(userId, destination, payload);
    }

    // ─── CONVENIENCE BUILDERS ─────────────────────────────────────────────

    /**
     * Derives alert severity from an engagement score and pushes it.
     * Engagement < 0.30 → CRITICAL, < 0.55 → WARNING, else → INFO
     */
    public void sendEngagementAlert(String sessionId,
                                     double engagementScore,
                                     double concentration) {
        AlertSeverity severity = engagementScore < 0.30 ? AlertSeverity.critical
                               : engagementScore < 0.55 ? AlertSeverity.warning
                                                        : AlertSeverity.info;

        AlertMessageDTO alert = new AlertMessageDTO(
                "ENGAGEMENT_DROP",
                "Engagement Drop Detected",
                String.format("Session engagement fell to %.0f%% (concentration: %.0f%%)",
                        engagementScore * 100, concentration * 100),
                severity,
                sessionId);

        sendAlertToSession(sessionId, alert);

        // Also push to global feed if critical
        if (severity == AlertSeverity.critical) {
            broadcastAlertGlobally(alert);
        }
    }
}