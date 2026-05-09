package com.eduvision.facade;

import com.eduvision.dto.lecturer.SessionHistoryDTO;
import com.eduvision.service.LecturerAnalyticsService;
import com.eduvision.service.LecturerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/facade/lecturer")
@PreAuthorize("hasRole('LECTURER')")
public class LecturerPortalFacadeController {

    private final LecturerService lecturerService;
    private final LecturerAnalyticsService analyticsService;

    public LecturerPortalFacadeController(LecturerService lecturerService,
                                           LecturerAnalyticsService analyticsService) {
        this.lecturerService = lecturerService;
        this.analyticsService = analyticsService;
    }

    /** GET /api/v1/facade/lecturer/dashboard */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        List<LecturerService.LecturerCourseDTO> courses = lecturerService.getCourses();
        List<SessionHistoryDTO> recentSessions = lecturerService.getSessionHistory()
                .stream().limit(5).toList();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("profile",         lecturerService.getProfile());
        dashboard.put("courses",         courses);
        dashboard.put("recentSessions",  recentSessions);
        dashboard.put("totalCourses",    courses.size());
        dashboard.put("totalSessions",   courses.stream().mapToInt(LecturerService.LecturerCourseDTO::getTotalSessions).sum());
        dashboard.put("activeSessions",  courses.stream().mapToInt(LecturerService.LecturerCourseDTO::getActiveSessions).sum());
        dashboard.put("activeSessionId", lecturerService.getActiveSessionId());

        return ResponseEntity.ok(dashboard);
    }

    /** GET /api/v1/facade/lecturer/sessions/{sessionId}/students */
    @GetMapping("/sessions/{sessionId}/students")
    public ResponseEntity<List<LecturerService.StudentSessionDTO>> getSessionStudents(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(lecturerService.getSessionStudents(sessionId));
    }

    /** GET /api/v1/facade/lecturer/sessions/{sessionId}/detected-students */
    @GetMapping("/sessions/{sessionId}/detected-students")
    public ResponseEntity<List<LecturerService.DetectedStudentDTO>> getDetectedStudents(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(lecturerService.getDetectedStudents(sessionId));
    }

    /** GET /api/v1/facade/lecturer/sessions/{sessionId}/focus-timeline */
    @GetMapping("/sessions/{sessionId}/focus-timeline")
    public ResponseEntity<List<Map<String, Object>>> getSessionFocusTimeline(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(analyticsService.getSessionFocusTimeline(sessionId));
    }

    /** GET /api/v1/facade/lecturer/courses/{courseId}/at-risk */
    @GetMapping("/courses/{courseId}/at-risk")
    public ResponseEntity<List<Map<String, Object>>> getCourseAtRiskStudents(
            @PathVariable String courseId) {
        return ResponseEntity.ok(analyticsService.getCourseAtRiskStudents(courseId));
    }

    /** GET /api/v1/facade/lecturer/courses/{courseId}/lecture-comparison */
    @GetMapping("/courses/{courseId}/lecture-comparison")
    public ResponseEntity<List<Map<String, Object>>> getCourseLectureComparison(
            @PathVariable String courseId) {
        return ResponseEntity.ok(analyticsService.getCourseLectureComparison(courseId));
    }

    /** GET /api/v1/facade/lecturer/courses/{courseId}/behavioral-patterns */
    @GetMapping("/courses/{courseId}/behavioral-patterns")
    public ResponseEntity<Map<String, Object>> getCourseBehavioralPatterns(
            @PathVariable String courseId) {
        return ResponseEntity.ok(analyticsService.getCourseBehavioralPatterns(courseId));
    }

    /** GET /api/v1/facade/lecturer/courses/{courseId}/ai-predictions */
    @GetMapping("/courses/{courseId}/ai-predictions")
    public ResponseEntity<List<Map<String, Object>>> getCourseAIPredictions(
            @PathVariable String courseId) {
        return ResponseEntity.ok(analyticsService.getCourseAIPredictions(courseId));
    }
}
