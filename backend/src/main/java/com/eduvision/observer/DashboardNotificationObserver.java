package com.eduvision.observer;

import com.eduvision.dto.websocket.AlertMessageDTO;
import com.eduvision.dto.websocket.MoodUpdateDTO;
import com.eduvision.model.AlertSeverity;
import com.eduvision.service.WebSocketNotificationService;
import org.springframework.stereotype.Component;

/**
 * OBSERVER PATTERN — subscriber of EmotionProcessingService.
 *
 * Every time the vision engine publishes a SentimentAlertEvent,
 * this observer converts it into WebSocket messages and pushes them
 * to the React dashboard in real-time via WebSocketNotificationService.
 *
 * Two messages are sent per event:
 *   1. MoodUpdateDTO  → /topic/session/{id}/mood   (always)
 *   2. AlertMessageDTO → /topic/session/{id}/alerts (only when engagement is low)
 */
@Component
public class DashboardNotificationObserver implements SentimentEventObserver {

    private final WebSocketNotificationService notificationService;

    public DashboardNotificationObserver(WebSocketNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void onEvent(SentimentAlertEvent event) {
        String sessionId      = event.getSessionId();
        double engagementScore = event.getEngagementScore();
        double concentration   = event.getConcentration();

        // ── 1. Push live mood update (always) ────────────────────────────
        MoodUpdateDTO mood = new MoodUpdateDTO(
                sessionId,
                deriveDominantEmotion(engagementScore),  // approximate from score
                engagementScore,
                concentration,
                0);    // studentCount not in SentimentAlertEvent — defaults to 0
                       // (override with EmotionSnapshot.studentCount when richer event exists)

        notificationService.sendMoodUpdate(sessionId, mood);

        // ── 2. Push alert only when engagement drops below threshold ──────
        if (engagementScore < 0.55) {
            AlertSeverity severity = engagementScore < 0.30
                    ? AlertSeverity.critical
                    : AlertSeverity.warning;

            String title   = severity == AlertSeverity.critical
                    ? "Critical: Very Low Engagement"
                    : "Warning: Engagement Dropping";

            String message = String.format(
                    "Session engagement is %.0f%% with %.0f%% concentration. " +
                    "Consider pausing to re-engage students.",
                    engagementScore * 100, concentration * 100);

            AlertMessageDTO alert = new AlertMessageDTO(
                    "ENGAGEMENT_DROP", title, message, severity, sessionId);

            notificationService.sendAlertToSession(sessionId, alert);

            // Escalate critical alerts to the global feed
            if (severity == AlertSeverity.critical) {
                notificationService.broadcastAlertGlobally(alert);
            }
        }
    }

    /**
     * Derives a rough dominant emotion label from an engagement score.
     * Replace this with the real EmotionType once SentimentAlertEvent
     * is extended to carry dominant emotion from the vision engine.
     */
    private String deriveDominantEmotion(double engagementScore) {
        if (engagementScore >= 0.75) return "engaged";
        if (engagementScore >= 0.55) return "neutral";
        if (engagementScore >= 0.35) return "confused";
        return "sad";
    }
}