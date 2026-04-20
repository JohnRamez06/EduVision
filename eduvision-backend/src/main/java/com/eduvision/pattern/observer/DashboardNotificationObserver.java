package com.eduvision.pattern.observer;
import org.springframework.stereotype.Component;
@Component
public class DashboardNotificationObserver implements SentimentEventObserver {
    @Override
    public void onEvent(SentimentAlertEvent event) {
        System.out.println("Dashboard: session " + event.getSessionId() + " engagement=" + event.getEngagementScore());
    }
}
