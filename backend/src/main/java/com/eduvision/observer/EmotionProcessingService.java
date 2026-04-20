package com.eduvision.observer;
import org.springframework.stereotype.Service;
import java.util.*;
@Service
public class EmotionProcessingService {
    private final List<SentimentEventObserver> observers = new ArrayList<>();
    public void subscribe(SentimentEventObserver observer) { observers.add(observer); }
    public void unsubscribe(SentimentEventObserver observer) { observers.remove(observer); }
    public void publish(SentimentAlertEvent event) { observers.forEach(o -> o.onEvent(event)); }
}
