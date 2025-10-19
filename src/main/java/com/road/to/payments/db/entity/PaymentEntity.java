package com.road.to.payments.db.entity;

import com.road.to.payments.model.PaymentType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentEntity {
    private Long id;
    private BigDecimal sum;
    private PaymentType type;
    private LocalDate date;
}
