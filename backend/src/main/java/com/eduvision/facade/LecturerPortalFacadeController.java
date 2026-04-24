package com.eduvision.facade;

import com.eduvision.dto.lecturer.SessionHistoryDTO;
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

    public LecturerPortalFacadeController(LecturerService lecturerService) {
        this.lecturerService = lecturerService;
    }

    /** GET /api/v1/facade/lecturer/dashboard */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        List<LecturerService.LecturerCourseDTO> courses = lecturerService.getCourses();
        List<SessionHistoryDTO> recentSessions = lecturerService.getSessionHistory()
                .stream().limit(5).toList();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("profile",        lecturerService.getProfile());
        dashboard.put("courses",         courses);
        dashboard.put("recentSessions",  recentSessions);
        dashboard.put("totalCourses",    courses.size());
        dashboard.put("totalSessions",   courses.stream().mapToInt(LecturerService.LecturerCourseDTO::getTotalSessions).sum());
        dashboard.put("activeSessions",  courses.stream().mapToInt(LecturerService.LecturerCourseDTO::getActiveSessions).sum());

        return ResponseEntity.ok(dashboard);
    }

    /** GET /api/v1/facade/lecturer/sessions/{sessionId}/students */
    @GetMapping("/sessions/{sessionId}/students")
    public ResponseEntity<List<LecturerService.StudentSessionDTO>> getSessionStudents(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(lecturerService.getSessionStudents(sessionId));
    }
}
