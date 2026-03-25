package com.innowise.user.mapper;

import com.innowise.user.dto.card.PaymentCardRequestDto;
import com.innowise.user.dto.card.PaymentCardResponseDto;
import com.innowise.user.entity.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {

    PaymentCard toEntity(PaymentCardRequestDto dto);

    @Mapping(source = "user.id", target = "userId")
    PaymentCardResponseDto toDto(PaymentCard card);
}
