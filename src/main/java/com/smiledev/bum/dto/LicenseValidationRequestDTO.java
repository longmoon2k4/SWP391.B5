package com.smiledev.bum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicenseValidationRequestDTO {
    private String licenseKey;
    private String hardwareId; // Optional for hardware-locked licenses
}
