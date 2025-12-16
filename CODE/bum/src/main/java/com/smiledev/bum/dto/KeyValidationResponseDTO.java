package com.smiledev.bum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeyValidationResponseDTO {
    private boolean valid;
    private String status; // "active", "revoked", "expired", "invalid"
    private String productName;
    private String developerName;
    private String message;
    private Long remainingRequests; // If rate limited
    private String expiresAt;
}
