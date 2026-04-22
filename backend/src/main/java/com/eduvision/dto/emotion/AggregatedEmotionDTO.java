package com.eduvision.dto.emotion;

import java.util.ArrayList;
import java.util.List;

public class AggregatedEmotionDTO {
    private EmotionSnapshotDTO classSnapshot;
    private List<StudentEmotionDTO> studentSnapshots = new ArrayList<>();

    public EmotionSnapshotDTO getClassSnapshot() {
        return classSnapshot;
    }

    public void setClassSnapshot(EmotionSnapshotDTO classSnapshot) {
        this.classSnapshot = classSnapshot;
    }

    public List<StudentEmotionDTO> getStudentSnapshots() {
        return studentSnapshots;
    }

    public void setStudentSnapshots(List<StudentEmotionDTO> studentSnapshots) {
        this.studentSnapshots = studentSnapshots;
    }
}
