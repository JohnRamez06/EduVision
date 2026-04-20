package com.eduvision.observer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ObserverPatternTest {
    @Test void observerReceivesEvent() {
        EmotionProcessingService publisher = new EmotionProcessingService();
        boolean[] received = {false};
        publisher.subscribe(event -> received[0] = true);
        publisher.publish(new SentimentAlertEvent("s1", 0.3, 0.2));
        assertTrue(received[0]);
    }
}
