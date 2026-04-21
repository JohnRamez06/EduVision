package com.eduvision.strategy.alert;
import org.springframework.stereotype.Component;
@Component("TrendAlertStrategy")
public class TrendAlertStrategy implements AlertStrategy {
    @Override
    public boolean shouldAlert(double concentration, double threshold) {
        return false;
    }
}