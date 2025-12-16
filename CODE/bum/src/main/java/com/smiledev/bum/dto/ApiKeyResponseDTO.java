package com.smiledev.bum.dto;

import com.smiledev.bum.entity.ApiKeys;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyResponseDTO {
    private Integer keyId;
    private String keyName;
    private String apiKey; // Only shown once on creation
    private String status;
    private Integer rateLimit;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime lastUsedAt;
    private Long totalRequests;
    private String masked; // Shows like: sk_live_****abcd1234

    public static ApiKeyResponseDTO fromEntity(ApiKeys key, boolean showSecret) {
        ApiKeyResponseDTO dto = new ApiKeyResponseDTO();
        dto.setKeyId(key.getKeyId());
        dto.setKeyName(key.getKeyName());
        dto.setStatus(key.getStatus().name());
        dto.setRateLimit(key.getRateLimit());
        dto.setCreatedAt(key.getCreatedAt());
        dto.setExpiresAt(key.getExpiresAt());
        dto.setLastUsedAt(key.getLastUsedAt());
        dto.setTotalRequests(key.getTotalRequests());
        
        if (showSecret) {
            dto.setApiKey(key.getApiKey());
        } else {
            String full = key.getApiKey();
            String masked = "sk_live_" + (full.length() > 4 ? "*".repeat(full.length() - 4) + full.substring(full.length() - 4) : full);
            dto.setMasked(masked);
        }
        
        return dto;
    }
}
