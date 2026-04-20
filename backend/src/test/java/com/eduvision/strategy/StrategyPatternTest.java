package com.eduvision.strategy;
import com.eduvision.strategy.alert.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class StrategyPatternTest {
    @Test void thresholdFiresWhenEngagementLow() {
        AlertStrategy s = new ThresholdAlertStrategy();
        assertTrue(s.shouldAlert(0.2, 0.5));
    }
    @Test void thresholdDoesNotFireWhenEngagementHigh() {
        AlertStrategy s = new ThresholdAlertStrategy();
        assertFalse(s.shouldAlert(0.8, 0.9));
    }
}
