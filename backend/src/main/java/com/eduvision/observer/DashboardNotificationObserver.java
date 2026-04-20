package com.eduvision.observer;
import org.springframework.stereotype.Component;
@Component
public class DashboardNotificationObserver implements SentimentEventObserver {
    @Override
    public void onEvent(SentimentAlertEvent event) {
        // TODO: implement DashboardNotificationObserver logic
        System.out.println("DashboardNotificationObserver received event for session: " + event.getSessionId());
    }
}
