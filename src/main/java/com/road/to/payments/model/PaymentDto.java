package com.road.to.payments.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentDto {
    BigDecimal sum;
    PaymentType type;
    LocalDate date;
}
