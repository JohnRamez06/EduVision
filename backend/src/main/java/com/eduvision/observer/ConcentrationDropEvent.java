package com.eduvision.observer;

public class ConcentrationDropEvent {

    private final String sessionId;
    private final String snapshotId;
    private final double previousConcentration;
    private final double currentConcentration;

    public ConcentrationDropEvent(String sessionId, String snapshotId,
                                  double previousConcentration, double currentConcentration) {
        this.sessionId = sessionId;
        this.snapshotId = snapshotId;
        this.previousConcentration = previousConcentration;
        this.currentConcentration = currentConcentration;
    }

    public String getSessionId() { return sessionId; }
    public String getSnapshotId() { return snapshotId; }
    public double getPreviousConcentration() { return previousConcentration; }
    public double getCurrentConcentration() { return currentConcentration; }
}
