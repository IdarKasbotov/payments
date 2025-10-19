package com.road.to.payments.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@ToString
@Schema(description = "Запрос на обновление суммы выплаты")
public class UpdatePaymentRequest {

    @Schema(description = "Сумма выплаты")
    @NotNull(message = "Сумма не может быть пустой")
    @PositiveOrZero(message = "Сумма не может быть отрицательной")
    private BigDecimal sum;

    @Schema(description = "Идентификатор выплаты")
    @NotNull(message = "Идентификатор не может быть пустым")
    private Long id;

}
