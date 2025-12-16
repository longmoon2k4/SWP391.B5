package com.smiledev.bum.service;

import com.smiledev.bum.dto.LicenseValidationRequestDTO;
import com.smiledev.bum.dto.LicenseValidationResponseDTO;
import com.smiledev.bum.entity.ApiKeys;
import com.smiledev.bum.entity.ApiUsageLogs;
import com.smiledev.bum.entity.Licenses;
import com.smiledev.bum.repository.ApiKeysRepository;
import com.smiledev.bum.repository.ApiUsageLogsRepository;
import com.smiledev.bum.repository.LicensesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class LicenseService {

    @Autowired
    private LicensesRepository licensesRepository;

    @Autowired
    private ApiKeysRepository apiKeysRepository;

    @Autowired
    private ApiUsageLogsRepository apiUsageLogsRepository;

    /**
     * Validate license key (called by developer's app)
     * Requires valid API key in Authorization header
     */
    public LicenseValidationResponseDTO validateLicense(String apiKey, LicenseValidationRequestDTO request) {
        // 1. Verify API key is valid
        Optional<ApiKeys> apiKeyOpt = apiKeysRepository.findByApiKey(apiKey);
        if (apiKeyOpt.isEmpty() || apiKeyOpt.get().getStatus() != ApiKeys.Status.active) {
            return new LicenseValidationResponseDTO(
                false, "invalid", "Invalid or inactive API key", null, null, null, null
            );
        }

        ApiKeys validApiKey = apiKeyOpt.get();
        Integer developerId = validApiKey.getDeveloper().getUserId();

        // 2. Find license key
        Optional<Licenses> licenseOpt = licensesRepository.findByLicenseKey(request.getLicenseKey());
        if (licenseOpt.isEmpty()) {
            return new LicenseValidationResponseDTO(
                false, "not_found", "License key not found", null, null, null, null
            );
        }

        Licenses license = licenseOpt.get();

        // 3. Verify license belongs to developer's product
        if (license.getProduct().getDeveloper().getUserId() != developerId) {
            return new LicenseValidationResponseDTO(
                false, "unauthorized", "License does not belong to your product", null, null, null, null
            );
        }

        // 4. Check license status
        if (license.getStatus() == Licenses.Status.banned) {
            return new LicenseValidationResponseDTO(
                false, "banned", "License has been banned", 
                license.getProduct().getName(), null, null, String.valueOf(license.getUser().getUserId())
            );
        }

        if (license.getStatus() == Licenses.Status.expired || 
            (license.getExpireDate() != null && license.getExpireDate().isBefore(LocalDateTime.now()))) {
            return new LicenseValidationResponseDTO(
                false, "expired", "License has expired", 
                license.getProduct().getName(), 
                formatDate(license.getExpireDate()), 
                license.getHardwareId(), 
                String.valueOf(license.getUser().getUserId())
            );
        }

        // 5. Check hardware ID if license is hardware-locked
        if (license.getHardwareId() != null && !license.getHardwareId().isEmpty()) {
            if (request.getHardwareId() == null || !license.getHardwareId().equals(request.getHardwareId())) {
                return new LicenseValidationResponseDTO(
                    false, "hardware_mismatch", "Hardware ID does not match", 
                    license.getProduct().getName(), null, license.getHardwareId(), null
                );
            }
        }

        // 6. Activate unused license if hardware ID provided
        if (license.getStatus() == Licenses.Status.unused && request.getHardwareId() != null) {
            license.setHardwareId(request.getHardwareId());
            license.setStatus(Licenses.Status.active);
            license.setStartDate(LocalDateTime.now());

            // Apply package duration on first activation; null duration means lifetime
            Integer durationDays = license.getProductPackage() != null ? license.getProductPackage().getDurationDays() : null;
            if (durationDays != null && license.getExpireDate() == null) {
                license.setExpireDate(license.getStartDate().plusDays(durationDays));
            }
            licensesRepository.save(license);
        }

        // 7. Increment API key usage
        validApiKey.setLastUsedAt(LocalDateTime.now());
        validApiKey.setTotalRequests(validApiKey.getTotalRequests() + 1);
        apiKeysRepository.save(validApiKey);

        // Log API usage
        logApiUsage(validApiKey, "/api/v1/licenses/validate", "POST", 200, null);

        // 8. License is valid
        return new LicenseValidationResponseDTO(
            true, 
            license.getStatus().name(), 
            "License is valid", 
            license.getProduct().getName(),
            formatDate(license.getExpireDate()),
            license.getHardwareId(),
            String.valueOf(license.getUser().getUserId())
        );
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "Never";
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private void logApiUsage(ApiKeys apiKey, String endpoint, String method, int statusCode, String errorMsg) {
        ApiUsageLogs log = new ApiUsageLogs();
        log.setApiKey(apiKey);
        log.setEndpoint(endpoint);
        log.setMethod(method);
        log.setStatusCode(statusCode);
        log.setErrorMessage(errorMsg);
        log.setRequestTime(LocalDateTime.now());
        apiUsageLogsRepository.save(log);
    }
}
