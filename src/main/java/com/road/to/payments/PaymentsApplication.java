package com.road.to.payments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
//        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
//        org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration.class
})
@EnableScheduling
public class PaymentsApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentsApplication.class, args);
	}

}
