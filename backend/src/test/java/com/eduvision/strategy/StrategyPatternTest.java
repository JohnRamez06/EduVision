package com.eduvision.strategy;
import com.eduvision.dto.alert.AlertContext;
import com.eduvision.strategy.alert.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class StrategyPatternTest {
    @Test void thresholdFiresWhenEngagementLow() {
        AlertStrategy s = new ThresholdAlertStrategy();
        AlertContext context = new AlertContext();
        context.setEngagementScore(0.2);
        context.setConcentrationLevel(0.5);
        assertTrue(s.shouldTrigger(context));
    }
    @Test void thresholdDoesNotFireWhenEngagementHigh() {
        AlertStrategy s = new ThresholdAlertStrategy();
        AlertContext context = new AlertContext();
        context.setEngagementScore(0.8);
        context.setConcentrationLevel(0.9);
        assertFalse(s.shouldTrigger(context));
    }
}