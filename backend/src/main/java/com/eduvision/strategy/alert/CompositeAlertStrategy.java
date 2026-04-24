package com.eduvision.strategy.alert;

import com.eduvision.dto.alert.AlertContext;
import com.eduvision.model.Alert;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CompositeAlertStrategy implements AlertStrategy {

    private final List<AlertStrategy> strategies;

    public CompositeAlertStrategy(List<AlertStrategy> strategies) {
        this.strategies = strategies;
    }

    @Override
    public boolean shouldAlert(double engagementScore, double concentration) {
        return strategies.stream()
                .filter(strategy -> !(strategy instanceof CompositeAlertStrategy))
                .anyMatch(strategy -> strategy.shouldAlert(engagementScore, concentration));
    }

    @Override
    public boolean shouldTrigger(AlertContext context) {
        return strategies.stream()
                .filter(strategy -> !(strategy instanceof CompositeAlertStrategy))
                .anyMatch(strategy -> strategy.shouldTrigger(context));
    }

    @Override
    public Alert createAlert(AlertContext context) {
        return strategies.stream()
                .filter(strategy -> !(strategy instanceof CompositeAlertStrategy))
                .filter(strategy -> strategy.shouldTrigger(context))
                .findFirst()
                .map(strategy -> strategy.createAlert(context))
                .orElse(null);
    }
}