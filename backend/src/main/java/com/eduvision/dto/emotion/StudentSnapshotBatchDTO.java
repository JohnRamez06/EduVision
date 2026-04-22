package com.eduvision.dto.emotion;

import java.util.List;

public class StudentSnapshotBatchDTO {

    private String snapshotId;
    private String sessionId;
    private List<StudentEmotionDTO> studentSnapshots;

    public String getSnapshotId() { return snapshotId; }
    public void setSnapshotId(String snapshotId) { this.snapshotId = snapshotId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public List<StudentEmotionDTO> getStudentSnapshots() { return studentSnapshots; }
    public void setStudentSnapshots(List<StudentEmotionDTO> studentSnapshots) { this.studentSnapshots = studentSnapshots; }
}
