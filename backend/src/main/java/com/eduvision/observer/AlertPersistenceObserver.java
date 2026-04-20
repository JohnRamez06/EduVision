package com.eduvision.observer;
import org.springframework.stereotype.Component;
@Component
public class AlertPersistenceObserver implements SentimentEventObserver {
    @Override
    public void onEvent(SentimentAlertEvent event) {
        // TODO: implement AlertPersistenceObserver logic
        System.out.println("AlertPersistenceObserver received event for session: " + event.getSessionId());
    }
}
