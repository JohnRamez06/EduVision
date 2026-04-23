package com.eduvision.facade;

import com.eduvision.dto.lecturer.LecturerDashboardDTO;
import com.eduvision.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/facade/dashboard")
public class DashboardFacadeController {

    private final DashboardService dashboardService;

    public DashboardFacadeController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/lecturer/{sessionId}")
    public ResponseEntity<LecturerDashboardDTO> getLecturerDashboard(@PathVariable("sessionId") String sessionId) {
        return ResponseEntity.ok(dashboardService.getLecturerDashboard(sessionId));
    }
}

