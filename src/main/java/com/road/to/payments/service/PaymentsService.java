package com.road.to.payments.service;

import com.road.to.payments.model.GetBalanceByTypeAndDateResponse;
import com.road.to.payments.model.GetPaymentsByTypeAndDateRequest;
import com.road.to.payments.model.GetPaymentsByTypeAndDateResponse;
import com.road.to.payments.model.PaymentDto;
import com.road.to.payments.model.PaymentType;
import com.road.to.payments.model.UpdatePaymentRequest;

import java.time.LocalDate;

public interface PaymentsService {

    GetPaymentsByTypeAndDateResponse getPaymentsByTypeAndDate(GetPaymentsByTypeAndDateRequest request);

    GetBalanceByTypeAndDateResponse getBalanceByTypeAndDate(PaymentType type, LocalDate date);

    void updatePaymentSum(UpdatePaymentRequest request);

    void savePayment(PaymentDto paymentDto);

    void countBalancesByTypeAndDate();

}
