package com.eduvision.strategy.alert;
import org.springframework.stereotype.Component;
@Component("ThresholdAlertStrategy")
public class ThresholdAlertStrategy implements AlertStrategy {
    @Override
    public boolean shouldAlert(double concentration, double threshold) {
        return concentration < threshold;
    }
}