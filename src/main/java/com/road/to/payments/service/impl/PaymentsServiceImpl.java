package com.road.to.payments.service.impl;

import com.road.to.payments.db.entity.PaymentEntity;
import com.road.to.payments.db.repository.BalancesRepository;
import com.road.to.payments.db.repository.PaymentRepository;
import com.road.to.payments.mapper.BalanceMapper;
import com.road.to.payments.mapper.PaymentMapper;
import com.road.to.payments.model.GetBalanceByTypeAndDateResponse;
import com.road.to.payments.model.GetPaymentsByTypeAndDateRequest;
import com.road.to.payments.model.GetPaymentsByTypeAndDateResponse;
import com.road.to.payments.model.PaymentDto;
import com.road.to.payments.model.PaymentType;
import com.road.to.payments.model.UpdatePaymentRequest;
import com.road.to.payments.service.PaymentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentsServiceImpl implements PaymentsService {

    private final PaymentRepository paymentRepository;
    private final BalancesRepository balancesRepository;

    private final PaymentMapper paymentMapper;
    private final BalanceMapper balanceMapper;

    @Override
    public GetPaymentsByTypeAndDateResponse getPaymentsByTypeAndDate(GetPaymentsByTypeAndDateRequest request) {
        log.debug("Получен запрос на получение выплат по типу {} и дате {}", request.getType(), request.getDate());
        List<PaymentDto> payments = paymentRepository.findPaymentByTypeAndDate(request.getType(), request.getDate())
                .stream()
                .map(paymentMapper::dtoFromEntity)
                .toList();
        log.debug("Найдены выплаты: {}", payments);

        return new GetPaymentsByTypeAndDateResponse(payments);
    }

    @Override
    public GetBalanceByTypeAndDateResponse getBalanceByTypeAndDate(PaymentType type, LocalDate date) {
        log.debug("Получен запрос на получение баланса по типу {} и дате {}", type, date);
        return balancesRepository.findBalanceByDateAndType(type, date)
                .map(GetBalanceByTypeAndDateResponse::new)
               .orElseGet(() -> new GetBalanceByTypeAndDateResponse(null));
    }

    @Override
    public void updatePaymentSum(UpdatePaymentRequest request) {
        log.debug("Получен запрос на обновление суммы выплаты: {}", request);
        paymentRepository.updatePaymentSum(request.getId(), request.getSum());
        log.debug("Сумма выплаты обновлена: {}", request);
    }

    @Override
    public void savePayment(PaymentDto paymentDto) {
        log.debug("Получен запрос на сохранение выплаты: {}", paymentDto);
        PaymentEntity paymentEntity = paymentMapper.entityFromDto(paymentDto);
        paymentRepository.save(paymentEntity);
        log.debug("Выплата сохранена");
    }

    @Override
    public void countBalancesByTypeAndDate() {
        List<PaymentEntity> balances = getBalancesGroupedByTypeAndDate();
        balancesRepository.deleteAll();
        balancesRepository.save(balances.stream().map(balanceMapper::balanceEntityFromPaymentEntity).toList());
    }

    private List<PaymentEntity> getBalancesGroupedByTypeAndDate() {
        return paymentRepository.paymentsGroupedByTypeAndDate();
    }
}
