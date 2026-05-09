// C:\Users\john\Desktop\eduvision\backend\src\main\java\com\eduvision\facade\DeanAnalyticsFacadeController.java
package com.eduvision.facade;

import com.eduvision.service.DeanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/facade/dean")
@PreAuthorize("hasAnyRole('DEAN', 'ADMIN')")
public class DeanAnalyticsFacadeController {

    private static final Logger logger = LoggerFactory.getLogger(DeanAnalyticsFacadeController.class);
    private final DeanService deanService;

    public DeanAnalyticsFacadeController(DeanService deanService) {
        this.deanService = deanService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        logger.info("Dean dashboard requested");
        try {
            Map<String, Object> result = deanService.getDepartmentSummary();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error in dean dashboard: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/lecturer-performance")
    public ResponseEntity<List<Map<String, Object>>> getLecturerPerformance() {
        logger.info("Dean lecturer performance requested");
        try {
            List<Map<String, Object>> result = deanService.getLecturerPerformance();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error in lecturer performance: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/course-stats")
    public ResponseEntity<List<Map<String, Object>>> getCourseStats() {
        logger.info("Dean course stats requested");
        try {
            List<Map<String, Object>> result = deanService.getCourseStats();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error in course stats: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/weekly-trends")
    public ResponseEntity<List<Map<String, Object>>> getWeeklyTrends() {
        logger.info("Dean weekly trends requested");
        try {
            List<Map<String, Object>> result = deanService.getWeeklyTrends();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error in weekly trends: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/courses")
    public ResponseEntity<List<Map<String, Object>>> getCourses() {
        try {
            return ResponseEntity.ok(deanService.getCourses());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/reports/attendance")
    public ResponseEntity<Map<String, Object>> getAttendanceAnalytics() {
        try {
            return ResponseEntity.ok(deanService.getAttendanceAnalytics());
        } catch (Exception e) {
            logger.error("Error in attendance analytics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/reports/focus")
    public ResponseEntity<Map<String, Object>> getStudentFocusReport() {
        try {
            return ResponseEntity.ok(deanService.getStudentFocusReport());
        } catch (Exception e) {
            logger.error("Error in student focus report: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/reports/peak-activity")
    public ResponseEntity<Map<String, Object>> getPeakActivityReport() {
        try {
            return ResponseEntity.ok(deanService.getPeakActivityReport());
        } catch (Exception e) {
            logger.error("Error in peak activity report: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}