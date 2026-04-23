package com.eduvision.controller;

import com.eduvision.dto.lecturer.SessionHistoryDTO;
import com.eduvision.service.LecturerService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/lecturer")
public class LecturerController {

    private final LecturerService lecturerService;

    public LecturerController(LecturerService lecturerService) {
        this.lecturerService = lecturerService;
    }

    @GetMapping("/profile")
    public ResponseEntity<LecturerService.LecturerProfileDTO> getProfile() {
        return ResponseEntity.ok(lecturerService.getProfile());
    }

    @GetMapping("/courses")
    public ResponseEntity<List<LecturerService.LecturerCourseDTO>> getCourses() {
        return ResponseEntity.ok(lecturerService.getCourses());
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<SessionHistoryDTO>> getSessionHistory() {
        return ResponseEntity.ok(lecturerService.getSessionHistory());
    }

    @GetMapping("/session/{id}/students")
    public ResponseEntity<List<LecturerService.StudentSessionDTO>> getSessionStudents(@PathVariable("id") String sessionId) {
        return ResponseEntity.ok(lecturerService.getSessionStudents(sessionId));
    }
}

