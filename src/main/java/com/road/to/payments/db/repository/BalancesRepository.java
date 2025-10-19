package com.road.to.payments.db.repository;

import com.road.to.payments.db.entity.BalanceEntity;
import com.road.to.payments.model.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BalancesRepository {

    void deleteAll();

    void save(List<BalanceEntity> balances);

    Optional<BigDecimal> findBalanceByDateAndType(PaymentType type, LocalDate date);

}
