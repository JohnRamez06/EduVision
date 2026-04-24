package com.eduvision.controller;

import com.eduvision.dto.consent.ConsentRequestDTO;
import com.eduvision.dto.consent.ConsentStatusDTO;
import com.eduvision.service.ConsentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/consent")
public class ConsentController {

    @Autowired
    private ConsentService consentService;

    @GetMapping("/status/{studentId}")
    public ConsentStatusDTO getConsentStatus(@PathVariable String studentId) {
        return consentService.getConsentStatus(studentId);
    }

    @PostMapping("/grant")
    public void grantConsent(@RequestBody ConsentRequestDTO request, HttpServletRequest httpRequest, Authentication auth) {
        String studentId = auth.getName(); // Assume authenticated user
        String ip = httpRequest.getRemoteAddr();
        consentService.grantConsent(studentId, request.getPolicyId(), ip);
    }

    @PostMapping("/revoke")
    public void revokeConsent(@RequestBody ConsentRequestDTO request, Authentication auth) {
        String studentId = auth.getName();
        consentService.revokeConsent(studentId, request.getPolicyId());
    }
}