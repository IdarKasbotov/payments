
package com.road.to.payments.mapper;

import com.road.to.payments.db.entity.BalanceEntity;
import com.road.to.payments.db.entity.PaymentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BalanceMapper {

    @Mapping(target = "balance", source = "sum")
    BalanceEntity balanceEntityFromPaymentEntity(PaymentEntity payment);
}
