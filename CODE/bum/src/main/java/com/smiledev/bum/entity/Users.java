package com.smiledev.bum.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "Users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private int userId;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", columnDefinition = "ENUM('admin', 'developer', 'user')")
    private Role role;

    @Column(name = "wallet_balance", precision = 15, scale = 2)
    private BigDecimal walletBalance;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    // --- Relationships ---

    @OneToMany(mappedBy = "developer")
    private Set<Products> developedProducts;

    @OneToMany(mappedBy = "user")
    private Set<Orders> orders;

    @OneToMany(mappedBy = "user")
    private Set<Licenses> licenses;

    @OneToMany(mappedBy = "user")
    private Set<Transactions> transactions;

    @OneToMany(mappedBy = "user")
    private Set<Reviews> reviews;

    @OneToMany(mappedBy = "user")
    private Set<ActivityLogs> activityLogs;

    public int getUserId() {
        return userId;
    }

    // --- Getters and Setters ---

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public BigDecimal getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(BigDecimal walletBalance) {
        this.walletBalance = walletBalance;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<Products> getDevelopedProducts() {
        return developedProducts;
    }

    public void setDevelopedProducts(Set<Products> developedProducts) {
        this.developedProducts = developedProducts;
    }

    public Set<Orders> getOrders() {
        return orders;
    }

    public void setOrders(Set<Orders> orders) {
        this.orders = orders;
    }

    public Set<Licenses> getLicenses() {
        return licenses;
    }

    public void setLicenses(Set<Licenses> licenses) {
        this.licenses = licenses;
    }

    public Set<Transactions> getTransactions() {
        return transactions;
    }

    public void setTransactions(Set<Transactions> transactions) {
        this.transactions = transactions;
    }

    public Set<Reviews> getReviews() {
        return reviews;
    }

    public void setReviews(Set<Reviews> reviews) {
        this.reviews = reviews;
    }

    public Set<ActivityLogs> getActivityLogs() {
        return activityLogs;
    }

    public void setActivityLogs(Set<ActivityLogs> activityLogs) {
        this.activityLogs = activityLogs;
    }

    public enum Role {
        admin, developer, user
    }
}
