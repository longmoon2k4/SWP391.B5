package com.smiledev.bum.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "api_keys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeys {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer keyId;

    @ManyToOne
    @JoinColumn(name = "developer_id", nullable = false)
    private Users developer;

    @Column(nullable = false, unique = true)
    private String apiKey; // Actual secret key

    @Column(nullable = false)
    private String keyName; // Display name (e.g., "Production", "Testing")

    @Column(nullable = false)
    private String keySecret; // Hash of apiKey for storage

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.active;

    @Column(nullable = false)
    private Integer rateLimit = 1000; // Requests per hour

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime expiresAt; // null = never expires

    @Column
    private LocalDateTime lastUsedAt;

    @Column(nullable = false)
    private Long totalRequests = 0L;

    @OneToMany(mappedBy = "apiKey", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ApiUsageLogs> usageLogs;

    public enum Status {
        active, revoked, expired
    }
}
