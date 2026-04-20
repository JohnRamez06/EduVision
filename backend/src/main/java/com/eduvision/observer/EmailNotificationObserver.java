package com.eduvision.observer;
import org.springframework.stereotype.Component;
@Component
public class EmailNotificationObserver implements SentimentEventObserver {
    @Override
    public void onEvent(SentimentAlertEvent event) {
        // TODO: implement EmailNotificationObserver logic
        System.out.println("EmailNotificationObserver received event for session: " + event.getSessionId());
    }
}
