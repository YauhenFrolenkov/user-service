package com.innowise.user.mapper;

import com.innowise.user.dto.user.UserRequestDto;
import com.innowise.user.dto.user.UserResponseDto;
import com.innowise.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserRequestDto dto);

    UserResponseDto toDto(User user);
}
