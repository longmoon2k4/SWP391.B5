package com.smiledev.bum.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class DepositRequest {

    @NotNull(message = "Số tiền không được để trống.")
    @DecimalMin(value = "10000.0", message = "Số tiền nạp tối thiểu là 10,000 ₫.")
    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
