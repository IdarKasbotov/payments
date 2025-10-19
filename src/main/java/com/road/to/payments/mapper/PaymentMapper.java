package com.road.to.payments.mapper;

import com.road.to.payments.db.entity.PaymentEntity;
import com.road.to.payments.model.PaymentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentDto dtoFromEntity(PaymentEntity payment);

    @Mapping(target = "id", ignore = true)
    PaymentEntity entityFromDto(PaymentDto paymentDto);

}
