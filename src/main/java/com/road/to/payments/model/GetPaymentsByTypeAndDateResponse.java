package com.road.to.payments.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetPaymentsByTypeAndDateResponse {
    private List<PaymentDto> payments;
}
