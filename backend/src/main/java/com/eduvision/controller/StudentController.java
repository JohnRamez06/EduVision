package com.eduvision.controller;

import com.eduvision.dto.student.*;
import com.eduvision.model.User;
import com.eduvision.service.ConsentService;
import com.eduvision.service.StudentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    private final StudentService studentService;
    private final ConsentService consentService;

    public StudentController(StudentService studentService,
                              ConsentService consentService) {
        this.studentService = studentService;
        this.consentService = consentService;
    }

    /** GET /api/v1/student/profile */
    @GetMapping("/profile")
    public ResponseEntity<StudentDashboardDTO.StudentProfileDTO> getProfile() {
        return ResponseEntity.ok(studentService.getProfile());
    }

    /** GET /api/v1/student/courses */
    @GetMapping("/courses")
    public ResponseEntity<List<StudentDashboardDTO.EnrolledCourseDTO>> getCourses() {
        return ResponseEntity.ok(studentService.getEnrolledCourses());
    }

    /** GET /api/v1/student/summaries */
    @GetMapping("/summaries")
    public ResponseEntity<List<LectureSummaryDTO>> getAllSummaries() {
        return ResponseEntity.ok(studentService.getLectureSummaries());
    }

    /** GET /api/v1/student/summary/{sessionId} */
    @GetMapping("/summary/{sessionId}")
    public ResponseEntity<LectureSummaryDTO> getSummaryBySession(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(
                studentService.getLectureSummaryBySession(sessionId));
    }

    /**
     * POST /api/v1/student/consent
     * Body: { "policyId": "uuid", "action": "grant" | "revoke" }
     */
    @PostMapping("/consent")
    public ResponseEntity<Void> updateConsent(
            @Valid @RequestBody ConsentRequest request) {
        User student = studentService.getCurrentUser();
        if ("revoke".equalsIgnoreCase(request.getAction())) {
            consentService.revokeConsent(student.getId(), request.getPolicyId());
        } else {
            consentService.grantConsent(student.getId(), request.getPolicyId());
        }
        return ResponseEntity.noContent().build();   // 204
    }

    // ── Inner request record for consent endpoint ─────────────────────────
    public static class ConsentRequest {
        @NotBlank private String policyId;
        @NotBlank private String action;   // "grant" | "revoke"

        public String getPolicyId() { return policyId; }
        public void setPolicyId(String policyId) { this.policyId = policyId; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
    }
}