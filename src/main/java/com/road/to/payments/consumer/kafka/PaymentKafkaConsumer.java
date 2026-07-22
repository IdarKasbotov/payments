package com.road.to.payments.consumer.kafka;

import com.road.to.payments.model.PaymentDto;
import com.road.to.payments.service.PaymentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentKafkaConsumer {

    private static final String PAYMENT_TOPIC = "${app.kafka.payment-topic}";

    private final PaymentsService paymentsService;

    @KafkaListener(topics = PAYMENT_TOPIC)
    public void consume(PaymentDto payment) {
        log.info("Получено сообщение из топика [{}]: {}", PAYMENT_TOPIC, payment);
        paymentsService.savePayment(payment);
        log.info("Обработано сообщение из топика [{}]: {}", PAYMENT_TOPIC, payment);
    }
}
