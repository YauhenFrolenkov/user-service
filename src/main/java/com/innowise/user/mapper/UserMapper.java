package com.innowise.user.mapper;

import com.innowise.user.dto.user.UserRequestDto;
import com.innowise.user.dto.user.UserResponseDto;
import com.innowise.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {PaymentCardMapper.class})
public interface UserMapper {

    User toEntity(UserRequestDto dto);

    @Mapping(target = "cards", source = "cards")
    UserResponseDto toDto(User user);
}
