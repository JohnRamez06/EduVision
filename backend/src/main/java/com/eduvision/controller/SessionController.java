package com.eduvision.controller;

import com.eduvision.dto.session.SessionEndRequest;
import com.eduvision.dto.session.SessionStartRequest;
import com.eduvision.dto.session.SessionStatusDTO;
import com.eduvision.service.SessionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/start")
    public ResponseEntity<SessionStatusDTO> startSession(@Valid @RequestBody SessionStartRequest request) {
        return ResponseEntity.ok(sessionService.startSession(request));
    }

    @PostMapping("/{id}/end")
    public ResponseEntity<SessionStatusDTO> endSession(
            @PathVariable("id") String id,
            @Valid @RequestBody SessionEndRequest request) {
        if (!id.equals(request.getSessionId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Session ID in path and body must match");
        }
        return ResponseEntity.ok(sessionService.endSession(id, request));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<SessionStatusDTO> getSessionStatus(@PathVariable("id") String id) {
        return ResponseEntity.ok(sessionService.getSessionStatus(id));
    }

    @GetMapping("/active")
    public ResponseEntity<List<SessionStatusDTO>> getActiveSessions() {
        return ResponseEntity.ok(sessionService.getActiveSessions());
    }
}

