package com.smiledev.bum.controller;

import com.smiledev.bum.dto.KeyValidationResponseDTO;
import com.smiledev.bum.service.ApiKeyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*") // Allow cross-origin for external clients
public class ApiController {

    @Autowired
    private ApiKeyService apiKeyService;

    /**
     * Validate API key
     * POST /api/v1/validate-key
     * Header: Authorization: Bearer sk_live_xxxxx
     */
    @PostMapping("/validate-key")
    public ResponseEntity<KeyValidationResponseDTO> validateKey(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest request) {

        String apiKey = null;
        
        // Extract API key from Authorization header
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            apiKey = authHeader.substring(7);
        }

        if (apiKey == null) {
            return ResponseEntity.badRequest().body(
                new KeyValidationResponseDTO(false, "invalid", null, null, "Missing API key in Authorization header", null, null)
            );
        }

        String clientIp = request.getRemoteAddr();
        KeyValidationResponseDTO response = apiKeyService.validateApiKey(apiKey, clientIp);

        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\": \"API service is running\"}");
    }
}
