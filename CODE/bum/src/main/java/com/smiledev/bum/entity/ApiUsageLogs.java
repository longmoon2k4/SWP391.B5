package com.smiledev.bum.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_usage_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiUsageLogs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @ManyToOne
    @JoinColumn(name = "api_key_id", nullable = true)
    private ApiKeys apiKey; // Can be null for invalid key attempts

    @Column(nullable = false)
    private String endpoint; // e.g., "/api/v1/validate-key"

    @Column(nullable = false)
    private String method; // GET, POST, etc

    @Column(nullable = false)
    private Integer statusCode; // 200, 401, 429, etc

    @Column
    private String requestIp;

    @Column(nullable = false)
    private LocalDateTime requestTime = LocalDateTime.now();

    @Column
    private Long responseTimeMs; // Response time in milliseconds

    @Column
    private String errorMessage; // If request failed
}
