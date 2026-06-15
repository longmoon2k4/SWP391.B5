package com.smiledev.bum.service;

import com.smiledev.bum.dto.ApiKeyRequestDTO;
import com.smiledev.bum.dto.ApiKeyResponseDTO;
import com.smiledev.bum.dto.KeyValidationResponseDTO;
import com.smiledev.bum.entity.ApiKeys;
import com.smiledev.bum.entity.ApiUsageLogs;
import com.smiledev.bum.entity.Users;
import com.smiledev.bum.repository.ApiKeysRepository;
import com.smiledev.bum.repository.ApiUsageLogsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ApiKeyService {

    @Autowired
    private ApiKeysRepository apiKeysRepository;

    @Autowired
    private ApiUsageLogsRepository apiUsageLogsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Sinh API key mới cho developer
     */
    public ApiKeyResponseDTO generateApiKey(Users developer, ApiKeyRequestDTO request) {
        // Sinh unique key
        String apiKey = "sk_live_" + UUID.randomUUID().toString().replace("-", "");
        String keySecret = passwordEncoder.encode(apiKey);

        ApiKeys newKey = new ApiKeys();
        newKey.setDeveloper(developer);
        newKey.setApiKey(apiKey);
        newKey.setKeySecret(keySecret);
        newKey.setKeyName(request.getKeyName());
        newKey.setRateLimit(request.getRateLimit() != null ? request.getRateLimit() : 1000);
        newKey.setStatus(ApiKeys.Status.active);
        newKey.setCreatedAt(LocalDateTime.now());

        ApiKeys saved = apiKeysRepository.save(newKey);
        return ApiKeyResponseDTO.fromEntity(saved, true); // Show secret only on creation
    }

    /**
     * Validate API key (called by external clients)
     */
    public KeyValidationResponseDTO validateApiKey(String apiKey, String requestIp) {
        // Log usage
        Optional<ApiKeys> keyOpt = apiKeysRepository.findByApiKey(apiKey);
        
        if (keyOpt.isEmpty()) {
            logApiUsage(null, "/api/v1/validate-key", "POST", 401, requestIp, "Invalid API key");
            return new KeyValidationResponseDTO(false, "invalid", null, null, "API key not found", null, null);
        }

        ApiKeys key = keyOpt.get();

        // Check status
        if (key.getStatus() == ApiKeys.Status.revoked) {
            logApiUsage(key, "/api/v1/validate-key", "POST", 401, requestIp, "Key revoked");
            return new KeyValidationResponseDTO(false, "revoked", null, null, "API key has been revoked", null, null);
        }

        if (key.getStatus() == ApiKeys.Status.expired || 
            (key.getExpiresAt() != null && key.getExpiresAt().isBefore(LocalDateTime.now()))) {
            logApiUsage(key, "/api/v1/validate-key", "POST", 401, requestIp, "Key expired");
            return new KeyValidationResponseDTO(false, "expired", null, null, "API key has expired", null, null);
        }

        // Check rate limit (requests in last 1 hour)
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long requestsLastHour = apiUsageLogsRepository.countByKeyIdAndRequestTimeAfter(key.getKeyId(), oneHourAgo);
        
        if (requestsLastHour >= key.getRateLimit()) {
            logApiUsage(key, "/api/v1/validate-key", "POST", 429, requestIp, "Rate limit exceeded");
            return new KeyValidationResponseDTO(
                false, "rate_limited", null, null, 
                "Rate limit exceeded (max " + key.getRateLimit() + " per hour)", 
                0L, null
            );
        }

        // Update last used time & increment total requests
        key.setLastUsedAt(LocalDateTime.now());
        key.setTotalRequests(key.getTotalRequests() + 1);
        apiKeysRepository.save(key);

        // Log successful validation
        logApiUsage(key, "/api/v1/validate-key", "POST", 200, requestIp, null);

        long remainingRequests = key.getRateLimit() - requestsLastHour - 1;
        String expiresAt = key.getExpiresAt() != null ? key.getExpiresAt().toString() : null;

        return new KeyValidationResponseDTO(
            true, "active", 
            key.getDeveloper().getUsername(), 
            key.getDeveloper().getFullName(),
            "Valid API key",
            remainingRequests,
            expiresAt
        );
    }

    /**
     * Revoke API key
     */
    public void revokeApiKey(Integer keyId) {
        apiKeysRepository.findById(keyId).ifPresent(key -> {
            key.setStatus(ApiKeys.Status.revoked);
            apiKeysRepository.save(key);
        });
    }

    /**
     * Delete API key
     */
    public void deleteApiKey(Integer keyId) {
        apiKeysRepository.deleteById(keyId);
    }

    /**
     * Log API usage
     */
    private void logApiUsage(ApiKeys apiKey, String endpoint, String method, int statusCode, String ip, String errorMsg) {
        ApiUsageLogs log = new ApiUsageLogs();
        log.setApiKey(apiKey);
        log.setEndpoint(endpoint);
        log.setMethod(method);
        log.setStatusCode(statusCode);
        log.setRequestIp(ip);
        log.setErrorMessage(errorMsg);
        log.setRequestTime(LocalDateTime.now());
        apiUsageLogsRepository.save(log);
    }
}
