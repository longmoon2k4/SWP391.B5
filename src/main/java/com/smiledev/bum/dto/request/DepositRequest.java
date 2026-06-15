package com.smiledev.bum.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class DepositRequest {

    // Không để trống, tối thiểu 10,000 ₫
    @NotNull(message = "Số tiền không được để trống.")
    @DecimalMin(value = "10000.00", message = "Số tiền nạp tối thiểu là 10,000 ₫.")
    // Tối đa theo DECIMAL(15,2) để tránh lỗi out-of-range ở DB
    @DecimalMax(value = "9999999999999.99", message = "Số tiền nạp vượt mức cho phép.")
    // Đảm bảo đúng số chữ số nguyên và 2 chữ số thập phân
    @Digits(integer = 13, fraction = 2, message = "Số tiền chỉ được phép tối đa 13 chữ số nguyên và 2 chữ số thập phân.")
    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
