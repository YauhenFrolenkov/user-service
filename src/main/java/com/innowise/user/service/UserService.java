package com.innowise.user.service;

import com.innowise.user.dto.user.UserRequestDto;
import com.innowise.user.dto.user.UserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserService {

    UserResponseDto createUser(UserRequestDto dto);

    UserResponseDto getUserById(Long id);

    Page<UserResponseDto> getUsers(String firstName, String lastName, Pageable pageable);

    UserResponseDto updateUser(Long id, UserRequestDto dto);

    void activateUser(Long id);

    void deactivateUser(Long id);

    Optional<UserResponseDto> getUserByEmail(String email);

}
