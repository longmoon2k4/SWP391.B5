package com.smiledev.bum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicenseValidationResponseDTO {
    private boolean valid;
    private String status; // active, expired, banned, unused
    private String message;
    private String productName;
    private String expireDate;
    private String hardwareId;
    private String userId;
}
