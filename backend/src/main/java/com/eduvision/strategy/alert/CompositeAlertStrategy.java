package com.eduvision.strategy.alert;
import java.util.List;
import org.springframework.stereotype.Component;
@Component
public class CompositeAlertStrategy implements AlertStrategy {
    private final List<AlertStrategy> strategies;
    public CompositeAlertStrategy(List<AlertStrategy> strategies) { this.strategies = strategies; }
    @Override public boolean shouldAlert(double e, double c) { return strategies.stream().anyMatch(s -> s.shouldAlert(e, c)); }
}
