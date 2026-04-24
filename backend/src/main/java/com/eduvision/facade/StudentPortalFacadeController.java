package com.eduvision.facade;

import com.eduvision.dto.student.*;
import com.eduvision.service.RecommendationService;
import com.eduvision.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.eduvision.dto.student.RecommendationDTO;
import java.util.List;

/**
 * FACADE PATTERN
 * Combines multiple service calls into a single cohesive API response,
 * so the frontend avoids making 4-5 individual round trips.
 *
 * Mapped under /api/v1/facade/student  (matches existing DashboardFacadeController pattern)
 */
@RestController
@RequestMapping("/api/v1/facade/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentPortalFacadeController {

    private final StudentService        studentService;
    private final RecommendationService recommendationService;

    public StudentPortalFacadeController(StudentService studentService,
                                          RecommendationService recommendationService) {
        this.studentService        = studentService;
        this.recommendationService = recommendationService;
    }

    /**
     * GET /api/v1/facade/student/dashboard
     *
     * Aggregates in one call:
     *   1. Student profile
     *   2. Enrolled courses
     *   3. 5 most recent lecture summaries
     *   4. Overall engagement stats
     *   5. Personalised recommendations
     */
    @GetMapping("/dashboard")
    public ResponseEntity<StudentDashboardDTO> getDashboard() {

        String userId = studentService.getCurrentUser().getId();

        StudentDashboardDTO dashboard = new StudentDashboardDTO();
        dashboard.setStudentInfo(studentService.getProfile());
        dashboard.setEnrolledCourses(studentService.getEnrolledCourses());
        dashboard.setRecentSummaries(
                studentService.getLectureSummaries()
                        .stream().limit(5).toList());
        dashboard.setOverallStats(studentService.getOverallStats(userId));
        dashboard.setRecommendations(
                recommendationService.generateRecommendations(userId));

        return ResponseEntity.ok(dashboard);
    }

    /**
     * GET /api/v1/facade/student/lecture/{sessionId}/timeline
     *
     * Returns the per-snapshot concentration + emotion timeline for one session.
     * Used to render the timeline chart on the student's lecture detail page.
     */
    @GetMapping("/courses")
    public ResponseEntity<List<StudentDashboardDTO.EnrolledCourseDTO>> getCourses() {
        return ResponseEntity.ok(studentService.getEnrolledCourses());
    }

    @GetMapping("/summaries")
    public ResponseEntity<List<LectureSummaryDTO>> getSummaries() {
        return ResponseEntity.ok(studentService.getLectureSummaries());
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<RecommendationDTO>> getRecommendations() {
        String userId = studentService.getCurrentUser().getId();
        return ResponseEntity.ok(recommendationService.generateRecommendations(userId));
    }

    @GetMapping("/lecture/{sessionId}/timeline")
    public ResponseEntity<ConcentrationTimelineDTO> getConcentrationTimeline(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(
                studentService.getConcentrationTimeline(sessionId));
    }
}