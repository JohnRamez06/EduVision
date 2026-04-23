package com.eduvision.service;

import com.eduvision.dto.lecturer.LecturerDashboardDTO;
import com.eduvision.dto.lecturer.StudentRiskDTO;
import com.eduvision.exception.ResourceNotFoundException;
import com.eduvision.model.*;
import com.eduvision.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final SessionRepository sessionRepository;
    private final SessionAttendanceRepository attendanceRepository;
    private final AlertRepository alertRepository;
    private final StudentEmotionSnapshotRepository emotionSnapshotRepository;
    private final StudentLectureSummaryRepository summaryRepository;

    public DashboardService(
            SessionRepository sessionRepository,
            SessionAttendanceRepository attendanceRepository,
            AlertRepository alertRepository,
            StudentEmotionSnapshotRepository emotionSnapshotRepository,
            StudentLectureSummaryRepository summaryRepository) {
        this.sessionRepository = sessionRepository;
        this.attendanceRepository = attendanceRepository;
        this.alertRepository = alertRepository;
        this.emotionSnapshotRepository = emotionSnapshotRepository;
        this.summaryRepository = summaryRepository;
    }

    public LecturerDashboardDTO getLecturerDashboard(String sessionId) {
        LectureSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        LecturerDashboardDTO dto = new LecturerDashboardDTO();

        // Session info
        LecturerDashboardDTO.SessionInfoDTO sessionInfo = new LecturerDashboardDTO.SessionInfoDTO();
        sessionInfo.setSessionId(session.getId());
        sessionInfo.setCourseName(session.getCourse() != null ? session.getCourse().getTitle() : null);
        sessionInfo.setRoomLocation(session.getRoomLocation());
        sessionInfo.setStudentCount(attendanceRepository.countBySessionIdAndStatus(sessionId, AttendanceStatus.present));
        sessionInfo.setActiveTime(calculateActiveTime(session));
        dto.setSessionInfo(sessionInfo);

        // Current mood (placeholder - would aggregate from emotion snapshots)
        dto.setCurrentMood("Neutral"); // Placeholder

        // Concentration trend (placeholder - would be real-time data)
        dto.setConcentrationTrend(generateConcentrationTrend(sessionId));

        // At-risk students
        dto.setAtRiskStudents(getAtRiskStudents(sessionId));

        // Recent alerts
        dto.setRecentAlerts(getRecentAlerts(sessionId));

        return dto;
    }

    private long calculateActiveTime(LectureSession session) {
        if (session.getActualStart() == null) {
            return 0L;
        }
        LocalDateTime end = session.getActualEnd() != null ? session.getActualEnd() : LocalDateTime.now();
        return java.time.Duration.between(session.getActualStart(), end).toMinutes();
    }

    private List<Double> generateConcentrationTrend(String sessionId) {
        // Placeholder: generate mock concentration trend data
        List<Double> trend = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            trend.add(70.0 + Math.random() * 20.0); // Random values between 70-90
        }
        return trend;
    }

    private List<StudentRiskDTO> getAtRiskStudents(String sessionId) {
        // Get students with low concentration scores from summaries
        return summaryRepository
                .findBySession_Id(sessionId)
                .stream()
                .filter(summary -> summary.getAvgConcentration() != null && summary.getAvgConcentration().doubleValue() < 60.0)
                .map(summary -> {
                    StudentRiskDTO dto = new StudentRiskDTO();
                    dto.setStudentId(summary.getStudent().getId());
                    dto.setStudentName(summary.getStudent().getFirstName() + " " + summary.getStudent().getLastName());
                    dto.setRiskLevel("High");
                    dto.setConcentrationScore(summary.getAvgConcentration().doubleValue());
                    dto.setTimeAtRisk(30L); // Placeholder minutes
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private List<String> getRecentAlerts(String sessionId) {
        // Get recent alerts for this session
        return alertRepository
                .findBySession_Id(sessionId)
                .stream()
                .sorted((a, b) -> b.getTriggeredAt().compareTo(a.getTriggeredAt()))
                .limit(5)
                .map(alert -> alert.getMessage())
                .collect(Collectors.toList());
    }
}
