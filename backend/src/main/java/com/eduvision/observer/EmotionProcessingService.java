package com.eduvision.observer;

// observer/EmotionProcessingService.java  — add @PostConstruct registration

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmotionProcessingService {

    private final List<SentimentEventObserver> observers = new ArrayList<>();

    // Inject all observers via constructor (Spring auto-collects @Component implementations)
    private final DashboardNotificationObserver dashboardObserver;
    private final AlertPersistenceObserver      alertPersistenceObserver;
    private final EmailNotificationObserver     emailObserver;

    public EmotionProcessingService(DashboardNotificationObserver dashboardObserver,
                                     AlertPersistenceObserver alertPersistenceObserver,
                                     EmailNotificationObserver emailObserver) {
        this.dashboardObserver      = dashboardObserver;
        this.alertPersistenceObserver = alertPersistenceObserver;
        this.emailObserver          = emailObserver;
    }

    @PostConstruct
    public void registerObservers() {
        subscribe(dashboardObserver);
        subscribe(alertPersistenceObserver);
        subscribe(emailObserver);
    }

    public void subscribe(SentimentEventObserver observer)   { observers.add(observer); }
    public void unsubscribe(SentimentEventObserver observer) { observers.remove(observer); }
    public void publish(SentimentAlertEvent event)           { observers.forEach(o -> o.onEvent(event)); }
}