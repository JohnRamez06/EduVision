package com.eduvision.dto.lecturer;

import java.util.List;

public class LecturerDashboardDTO {

    private SessionInfoDTO sessionInfo;
    private String currentMood;
    private List<Double> concentrationTrend;
    private List<StudentRiskDTO> atRiskStudents;
    private List<String> recentAlerts;

    public SessionInfoDTO getSessionInfo() {
        return sessionInfo;
    }

    public void setSessionInfo(SessionInfoDTO sessionInfo) {
        this.sessionInfo = sessionInfo;
    }

    public String getCurrentMood() {
        return currentMood;
    }

    public void setCurrentMood(String currentMood) {
        this.currentMood = currentMood;
    }

    public List<Double> getConcentrationTrend() {
        return concentrationTrend;
    }

    public void setConcentrationTrend(List<Double> concentrationTrend) {
        this.concentrationTrend = concentrationTrend;
    }

    public List<StudentRiskDTO> getAtRiskStudents() {
        return atRiskStudents;
    }

    public void setAtRiskStudents(List<StudentRiskDTO> atRiskStudents) {
        this.atRiskStudents = atRiskStudents;
    }

    public List<String> getRecentAlerts() {
        return recentAlerts;
    }

    public void setRecentAlerts(List<String> recentAlerts) {
        this.recentAlerts = recentAlerts;
    }

    public static class SessionInfoDTO {
        private String sessionId;
        private String courseName;
        private String roomLocation;
        private int studentCount;
        private long activeTime;

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }

        public String getRoomLocation() {
            return roomLocation;
        }

        public void setRoomLocation(String roomLocation) {
            this.roomLocation = roomLocation;
        }

        public int getStudentCount() {
            return studentCount;
        }

        public void setStudentCount(int studentCount) {
            this.studentCount = studentCount;
        }

        public long getActiveTime() {
            return activeTime;
        }

        public void setActiveTime(long activeTime) {
            this.activeTime = activeTime;
        }
    }
}
