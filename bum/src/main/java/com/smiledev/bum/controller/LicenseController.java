package com.smiledev.bum.controller;

import com.smiledev.bum.dto.LicenseValidationRequestDTO;
import com.smiledev.bum.dto.LicenseValidationResponseDTO;
import com.smiledev.bum.service.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/licenses")
@CrossOrigin(origins = "*")
public class LicenseController {

    @Autowired
    private LicenseService licenseService;

    /**
     * Validate license key
     * POST /api/v1/licenses/validate
     * Header: Authorization: Bearer sk_live_xxxxx
     * Body: { "licenseKey": "XXXX-XXXX-XXXX-XXXX", "hardwareId": "optional" }
     */
    @PostMapping("/validate")
    public ResponseEntity<LicenseValidationResponseDTO> validateLicense(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody LicenseValidationRequestDTO request) {

        // Extract API key from Authorization header
        String apiKey = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            apiKey = authHeader.substring(7);
        }

        if (apiKey == null) {
            return ResponseEntity.badRequest().body(
                new LicenseValidationResponseDTO(false, "invalid", "Missing API key in Authorization header", null, null, null, null)
            );
        }

        if (request.getLicenseKey() == null || request.getLicenseKey().isEmpty()) {
            return ResponseEntity.badRequest().body(
                new LicenseValidationResponseDTO(false, "invalid", "Missing license key in request body", null, null, null, null)
            );
        }

        LicenseValidationResponseDTO response = licenseService.validateLicense(apiKey, request);
        return ResponseEntity.ok(response);
    }
}
