package com.road.to.payments.consumer.rabbit;

import com.road.to.payments.model.PaymentDto;
import com.road.to.payments.service.PaymentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.road.to.payments.config.RabbitConfig.PAYMENT_QUEUE;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentConsumer {

    private final PaymentsService paymentService;

    @RabbitListener(queues = PAYMENT_QUEUE)
    public void receiveMessage(PaymentDto message) {
        log.info("Получено сообщение в очереди {}: {}", PAYMENT_QUEUE, message);
        paymentService.savePayment(message);
    }

}
