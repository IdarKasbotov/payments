package com.road.to.payments.db.entity;

import com.road.to.payments.model.PaymentType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BalanceEntity {
    private LocalDate date;
    private PaymentType type;
    private BigDecimal balance;

    public String getType() {
        return type.name();
    }
}
