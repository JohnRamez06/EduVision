package com.eduvision.observer;

import com.eduvision.model.AlertSeverity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ObserverPatternTest {

    @Test
    void observerReceivesEvent() {
        EmotionProcessingService publisher = new EmotionProcessingService();
        boolean[] received = {false};
        publisher.subscribe(event -> received[0] = true);
        publisher.publish(new SentimentAlertEvent("s1", "snap1", 0.3, 0.2,
                AlertSeverity.warning, "test"));
        assertTrue(received[0]);
    }
}
