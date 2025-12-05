package com.smiledev.bum.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "Orders")
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private int orderId;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('pending', 'completed', 'failed', 'refunded')")
    private Status status;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    // --- Relationships ---

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @OneToMany(mappedBy = "order")
    private Set<Licenses> licenses;

    public int getOrderId() {
        return orderId;
    }

    // --- Getters and Setters ---

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Set<Licenses> getLicenses() {
        return licenses;
    }

    public void setLicenses(Set<Licenses> licenses) {
        this.licenses = licenses;
    }

    public enum Status {
        pending, completed, failed, refunded
    }
}
