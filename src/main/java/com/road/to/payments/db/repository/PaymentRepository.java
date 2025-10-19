package com.road.to.payments.db.repository;

import com.road.to.payments.db.entity.PaymentEntity;
import com.road.to.payments.model.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository {

    List<PaymentEntity> findPaymentByTypeAndDate(PaymentType type, LocalDate date);

    void updatePaymentSum(Long id, BigDecimal sum);

    void save(PaymentEntity payment);

    List<PaymentEntity> paymentsGroupedByTypeAndDate();
}
