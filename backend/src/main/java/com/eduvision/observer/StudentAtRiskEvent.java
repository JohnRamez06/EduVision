package com.eduvision.observer;

import com.eduvision.model.ConcentrationLevel;
import com.eduvision.model.EmotionType;

public class StudentAtRiskEvent {

    private final String sessionId;
    private final String studentId;
    private final String snapshotId;
    private final EmotionType emotion;
    private final ConcentrationLevel concentration;

    public StudentAtRiskEvent(String sessionId, String studentId, String snapshotId,
                              EmotionType emotion, ConcentrationLevel concentration) {
        this.sessionId = sessionId;
        this.studentId = studentId;
        this.snapshotId = snapshotId;
        this.emotion = emotion;
        this.concentration = concentration;
    }

    public String getSessionId() { return sessionId; }
    public String getStudentId() { return studentId; }
    public String getSnapshotId() { return snapshotId; }
    public EmotionType getEmotion() { return emotion; }
    public ConcentrationLevel getConcentration() { return concentration; }
}
