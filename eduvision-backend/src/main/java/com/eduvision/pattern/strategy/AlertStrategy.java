package com.eduvision.pattern.strategy;
public interface AlertStrategy {
    boolean shouldAlert(double engagementScore, double concentration);
}
