package com.smiledev.bum.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ProductVersions")
public class ProductVersions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "version_id")
    private int versionId;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Products product;

    @Column(name = "version_number", nullable = false, length = 20)
    private String versionNumber;

    @Column(name = "source_code_path", nullable = false, length = 255)
    private String sourceCodePath;

    @Column(name = "build_file_path", nullable = false, length = 255)
    private String buildFilePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "virus_scan_status", columnDefinition = "ENUM('pending', 'clean', 'infected')")
    private VirusScanStatus virusScanStatus;

    @Column(name = "virus_total_report_link", length = 255)
    private String virusTotalReportLink;

    @Column(name = "is_current_version")
    private boolean isCurrentVersion;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum VirusScanStatus {
        pending, clean, infected
    }

    // Getters and setters

    public int getVersionId() {
        return versionId;
    }

    public void setVersionId(int versionId) {
        this.versionId = versionId;
    }

    public Products getProduct() {
        return product;
    }

    public void setProduct(Products product) {
        this.product = product;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getSourceCodePath() {
        return sourceCodePath;
    }

    public void setSourceCodePath(String sourceCodePath) {
        this.sourceCodePath = sourceCodePath;
    }

    public String getBuildFilePath() {
        return buildFilePath;
    }

    public void setBuildFilePath(String buildFilePath) {
        this.buildFilePath = buildFilePath;
    }

    public VirusScanStatus getVirusScanStatus() {
        return virusScanStatus;
    }

    public void setVirusScanStatus(VirusScanStatus virusScanStatus) {
        this.virusScanStatus = virusScanStatus;
    }

    public String getVirusTotalReportLink() {
        return virusTotalReportLink;
    }

    public void setVirusTotalReportLink(String virusTotalReportLink) {
        this.virusTotalReportLink = virusTotalReportLink;
    }

    public boolean isCurrentVersion() {
        return isCurrentVersion;
    }

    public void setCurrentVersion(boolean currentVersion) {
        isCurrentVersion = currentVersion;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
