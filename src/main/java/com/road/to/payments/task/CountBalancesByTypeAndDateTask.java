package com.road.to.payments.task;

import com.road.to.payments.service.PaymentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CountBalancesByTypeAndDateTask {

    private final PaymentsService paymentsService;

    @Scheduled(cron = "${job.count-balances-by-type-and-date.cron}")
    public void countBalancesByTypeAndDate() {
        log.debug("Получен запрос на подсчет балансов по типу и дате");
        paymentsService.countBalancesByTypeAndDate();
        log.debug("Балансы подсчитаны");
    }
}
