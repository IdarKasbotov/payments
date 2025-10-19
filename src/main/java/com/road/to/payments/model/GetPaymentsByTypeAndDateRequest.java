package com.road.to.payments.model;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@ToString
@Schema(description = "Запрос получения списка выплат по типу и дате")
public class GetPaymentsByTypeAndDateRequest {

    @Schema(description = "Тип выплаты", implementation = PaymentType.class)
    @NotBlank
    private PaymentType type;

    @Schema(description = "Дата выплаты", example = "2025-10-01")
    @NotBlank
    private LocalDate date;

}
