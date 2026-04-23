package com.eduvision.dto.lecturer;

public class StudentRiskDTO {

    private String studentId;
    private String studentName;
    private String riskLevel;
    private double concentrationScore;
    private long timeAtRisk;

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public double getConcentrationScore() {
        return concentrationScore;
    }

    public void setConcentrationScore(double concentrationScore) {
        this.concentrationScore = concentrationScore;
    }

    public long getTimeAtRisk() {
        return timeAtRisk;
    }

    public void setTimeAtRisk(long timeAtRisk) {
        this.timeAtRisk = timeAtRisk;
    }
}
