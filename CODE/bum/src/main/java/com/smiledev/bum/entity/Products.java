package com.smiledev.bum.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "Products")
public class Products {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private int productId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Lob
    @Column(name = "description", columnDefinition = "MEDIUMTEXT")
    private String description;

    @Column(name = "short_description", length = 255)
    private String shortDescription;

    @Column(name = "demo_video_url", length = 255)
    private String demoVideoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('pending', 'approved', 'rejected', 'hidden')")
    private Status status;

    @Lob
    @Column(name = "rejection_reason", columnDefinition = "MEDIUMTEXT")
    private String rejectionReason;

    @Column(name = "total_sales")
    private int totalSales;

    @Column(name = "view_count")
    private int viewCount;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", updatable = false, insertable = false)
    private LocalDateTime updatedAt;

    // --- Relationships ---

    @ManyToOne
    @JoinColumn(name = "developer_id", nullable = false)
    private Users developer;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Categories category;

    @OneToMany(mappedBy = "product")
    private Set<ProductVersions> versions;

    @OneToMany(mappedBy = "product")
    private Set<ProductPackages> packages;

    @OneToMany(mappedBy = "product")
    private Set<Licenses> licenses;

    @OneToMany(mappedBy = "product")
    private Set<Reviews> reviews;

    public int getProductId() {
        return productId;
    }

    // --- Getters and Setters ---

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDemoVideoUrl() {
        return demoVideoUrl;
    }

    public void setDemoVideoUrl(String demoVideoUrl) {
        this.demoVideoUrl = demoVideoUrl;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public int getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(int totalSales) {
        this.totalSales = totalSales;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Users getDeveloper() {
        return developer;
    }

    public void setDeveloper(Users developer) {
        this.developer = developer;
    }

    public Categories getCategory() {
        return category;
    }

    public void setCategory(Categories category) {
        this.category = category;
    }

    public Set<ProductVersions> getVersions() {
        return versions;
    }

    public void setVersions(Set<ProductVersions> versions) {
        this.versions = versions;
    }

    public Set<ProductPackages> getPackages() {
        return packages;
    }

    public void setPackages(Set<ProductPackages> packages) {
        this.packages = packages;
    }

    public Set<Licenses> getLicenses() {
        return licenses;
    }

    public void setLicenses(Set<Licenses> licenses) {
        this.licenses = licenses;
    }

    public Set<Reviews> getReviews() {
        return reviews;
    }

    public void setReviews(Set<Reviews> reviews) {
        this.reviews = reviews;
    }

    public enum Status {
        pending, approved, rejected, hidden
    }
}
