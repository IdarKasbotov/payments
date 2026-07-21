package com.road.to.payments.controller;

import com.road.to.payments.model.GetBalanceByTypeAndDateResponse;
import com.road.to.payments.model.GetPaymentsByTypeAndDateRequest;
import com.road.to.payments.model.GetPaymentsByTypeAndDateResponse;
import com.road.to.payments.model.PaymentType;
import com.road.to.payments.model.UpdatePaymentRequest;
import com.road.to.payments.service.PaymentsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@AllArgsConstructor
@RequestMapping(PaymentsController.BASE_PREFIX)
@Tag(
        name = PaymentsController.TAG,
        description = "API для получения и обновления зарплат сотрудников"
)
@Validated
public class PaymentsController {

    public static final String BASE_PREFIX = "/payments";
    public static final String TAG = "payments-controller";

    private final PaymentsService paymentsService;

    @Operation(summary = "Получение платежей по типу и дате")
    @PostMapping(
            path = "/getPaymentsByTypeAndDate",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<GetPaymentsByTypeAndDateResponse> getPaymentsByTypeAndDate(@RequestBody @Valid GetPaymentsByTypeAndDateRequest request) {
        return ResponseEntity.ok(paymentsService.getPaymentsByTypeAndDate(request));
    }

    @Operation(summary = "Получение балансов по типу и дате")
    @GetMapping(path = "/getBalanceByTypeAndDate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetBalanceByTypeAndDateResponse> getBalanceByTypeAndDate(
            @RequestParam PaymentType type,
            @Parameter(
                    description = "Дата выплаты",
                    example = "2025-10-05"
            )
            @RequestParam LocalDate date) {
        return ResponseEntity.ok(paymentsService.getBalanceByTypeAndDate(type, date));
    }

    @Operation(summary = "Обновление суммы платежа")
    @PostMapping(
            path = "/updatePaymentSum",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public void updatePaymentSum(@RequestBody @Valid UpdatePaymentRequest request) {
        paymentsService.updatePaymentSum(request);
    }

}
