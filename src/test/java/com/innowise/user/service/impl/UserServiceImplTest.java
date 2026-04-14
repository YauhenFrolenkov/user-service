package com.innowise.user.service.impl;

import com.innowise.user.dto.user.UserRequestDto;
import com.innowise.user.dto.user.UserResponseDto;
import com.innowise.user.entity.User;
import com.innowise.user.exception.UserNotFoundException;
import com.innowise.user.mapper.UserMapper;
import com.innowise.user.repository.PaymentCardRepository;
import com.innowise.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserRequestDto userRequestDto;
    private UserResponseDto userResponseDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Yauhen");
        user.setEmail("yauhen@example.com");
        user.setActive(true);

        userRequestDto = new UserRequestDto();
        userRequestDto.setName("Yauhen");
        userRequestDto.setEmail("yauhen@example.com");

        userResponseDto = new UserResponseDto();
        userResponseDto.setId(1L);
        userResponseDto.setName("Yauhen");
        userResponseDto.setEmail("yauhen@example.com");
        userResponseDto.setActive(true);
    }

    @Test
    void testGetUserById_Success() {
        when(userRepository.findWithCardsById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userResponseDto);

        UserResponseDto result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Yauhen", result.getName());
        assertEquals("yauhen@example.com", result.getEmail());

        verify(userRepository, times(1)).findWithCardsById(1L);
        verify(userMapper, times(1)).toDto(user);
    }

    @Test
    void testGetUserById_NotFound() {

        when(userRepository.findWithCardsById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(1L));

        verify(userRepository, times(1)).findWithCardsById(1L);
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void testCreateUser_Success() {
        when(userMapper.toEntity(userRequestDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userResponseDto);

        UserResponseDto result = userService.createUser(userRequestDto);

        assertNotNull(result);
        assertEquals("Yauhen", result.getName());
        assertEquals("yauhen@example.com", result.getEmail());

        verify(userMapper, times(1)).toEntity(userRequestDto);
        verify(userRepository, times(1)).save(user);
        verify(userMapper, times(1)).toDto(user);
    }

    @Test
    void testGetUserByEmail_Found() {

        when(userRepository.findByEmail("yauhen@example.com"))
                .thenReturn(Optional.of(user));

        when(userMapper.toDto(user)).thenReturn(userResponseDto);

        Optional<UserResponseDto> result =
                userService.getUserByEmail("yauhen@example.com");

        assertTrue(result.isPresent());
        assertEquals("Yauhen", result.get().getName());

        verify(userRepository, times(1))
                .findByEmail("yauhen@example.com");

        verify(userMapper, times(1))
                .toDto(user);
    }

    @Test
    void testGetUserByEmail_NotFound() {

        when(userRepository.findByEmail("yauhen@example.com"))
                .thenReturn(Optional.empty());

        Optional<UserResponseDto> result =
                userService.getUserByEmail("yauhen@example.com");

        assertTrue(result.isEmpty());

        verify(userRepository, times(1))
                .findByEmail("yauhen@example.com");

        verify(userMapper, never()).toDto(any());
    }

    @Test
    void testUpdateUser_Success() {

        when(userRepository.findByIdIncludingInactive(1L)).thenReturn(Optional.of(user));

        when(userRepository.save(user)).thenReturn(user);

        when(userMapper.toDto(user)).thenReturn(userResponseDto);

        UserResponseDto result = userService.updateUser(1L, userRequestDto);

        assertNotNull(result);
        assertEquals("Yauhen", result.getName());

        verify(userRepository, times(1))
                .findByIdIncludingInactive(1L);

        verify(userRepository, times(1))
                .save(user);

        verify(userMapper, times(1))
                .toDto(user);
    }

    @Test
    void testActivateUser_Success() {

        when(userRepository.findByIdIncludingInactive(1L))
                .thenReturn(Optional.of(user));

        userService.activateUser(1L);

        assertTrue(user.getActive());

        verify(userRepository, times(1))
                .findByIdIncludingInactive(1L);

        verify(userRepository, times(1))
                .save(user);

        verify(paymentCardRepository, times(1))
                .activateCardsByUserId(1L);
    }

    @Test
    void testDeactivateUser_Success() {

        when(userRepository.findByIdIncludingInactive(1L))
                .thenReturn(Optional.of(user));

        userService.deactivateUser(1L);

        assertFalse(user.getActive());

        verify(userRepository, times(1))
                .findByIdIncludingInactive(1L);

        verify(userRepository, times(1))
                .save(user);

        verify(paymentCardRepository, times(1))
                .deactivateCardsByUserId(1L);
    }

    @Test
    void testGetUsers() {

        Page<User> userPage = new PageImpl<>(List.of(user));

        when(userRepository.findAll(
                ArgumentMatchers.<Specification<User>>any(),
                any(Pageable.class)))
                .thenReturn(userPage);

        when(userMapper.toDto(user))
                .thenReturn(userResponseDto);

        Page<UserResponseDto> result =
                userService.getUsers(null, null, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(userRepository, times(1))
                .findAll(
                        ArgumentMatchers.<Specification<User>>any(),
                        any(Pageable.class));
    }

    @Test
    void testUpdateUser_NotFound() {
        when(userRepository.findByIdIncludingInactive(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                userService.updateUser(1L, userRequestDto));
    }

    @Test
    void testActivateUser_NotFound() {
        when(userRepository.findByIdIncludingInactive(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.activateUser(1L));
    }

    @Test
    void testDeactivateUser_NotFound() {
        when(userRepository.findByIdIncludingInactive(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.deactivateUser(1L));
    }

    @Test
    void testGetUsers_Empty() {
        Page<User> emptyPage = new PageImpl<>(Collections.emptyList());
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

        Page<UserResponseDto> result = userService.getUsers(null, null, PageRequest.of(0, 10));
        assertTrue(result.isEmpty());
    }

    @Test
    void testActivateUser_AlreadyActive() {
        user.setActive(true);

        when(userRepository.findByIdIncludingInactive(1L))
                .thenReturn(Optional.of(user));

        userService.activateUser(1L);

        assertTrue(user.getActive());

        verify(userRepository, times(1)).save(user);
        verify(paymentCardRepository, times(1)).activateCardsByUserId(1L);

    }

    @Test
    void testDeactivateUser_AlreadyInactive() {
        user.setActive(false);

        when(userRepository.findByIdIncludingInactive(1L))
                .thenReturn(Optional.of(user));

        userService.deactivateUser(1L);

        assertFalse(user.getActive());

        verify(userRepository, times(1)).save(user);
        verify(paymentCardRepository, times(1)).deactivateCardsByUserId(1L);
    }

    @Test
    void testGetUsers_WithFilters() {
        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        when(userMapper.toDto(user)).thenReturn(userResponseDto);

        Page<UserResponseDto> result =
                userService.getUsers("Yauhen", "yauhen@example.com", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

}

