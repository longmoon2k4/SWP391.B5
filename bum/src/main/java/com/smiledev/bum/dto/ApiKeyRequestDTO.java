package com.smiledev.bum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyRequestDTO {
    private String keyName;
    private Integer rateLimit; // null = default 1000
}
