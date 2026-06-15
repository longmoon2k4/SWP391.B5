package com.smiledev.bum.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Set;

@Entity
@Table(name = "ProductPackages")
public class ProductPackages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "package_id")
    private int packageId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // --- Relationships ---

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Products product;

    @OneToMany(mappedBy = "productPackage")
    private Set<Licenses> licenses;

    // --- Getters and Setters ---

    public int getPackageId() {
        return packageId;
    }

    public void setPackageId(int packageId) {
        this.packageId = packageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Products getProduct() {
        return product;
    }

    public void setProduct(Products product) {
        this.product = product;
    }

    public Set<Licenses> getLicenses() {
        return licenses;
    }

    public void setLicenses(Set<Licenses> licenses) {
        this.licenses = licenses;
    }
}
