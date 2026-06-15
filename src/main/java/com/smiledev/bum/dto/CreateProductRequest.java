package com.smiledev.bum.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class CreateProductRequest {
    private String name;
    private Integer categoryId;
    private String shortDescription;
    private String description;
    private String demoVideoUrl;
    private MultipartFile exeFile;
    private String versionNumber;
    private List<PackageRequest> packages;

    public static class PackageRequest {
        private String name;
        private Double price;
        private Integer durationDays;
        private String description;

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public Integer getDurationDays() {
            return durationDays;
        }

        public void setDurationDays(Integer durationDays) {
            this.durationDays = durationDays;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDemoVideoUrl() {
        return demoVideoUrl;
    }

    public void setDemoVideoUrl(String demoVideoUrl) {
        this.demoVideoUrl = demoVideoUrl;
    }

    public MultipartFile getExeFile() {
        return exeFile;
    }

    public void setExeFile(MultipartFile exeFile) {
        this.exeFile = exeFile;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public List<PackageRequest> getPackages() {
        return packages;
    }

    public void setPackages(List<PackageRequest> packages) {
        this.packages = packages;
    }
}
