package com.innowise.user.service.impl;

import com.innowise.user.dto.user.UserRequestDto;
import com.innowise.user.dto.user.UserResponseDto;
import com.innowise.user.entity.User;
import com.innowise.user.exception.UserNotFoundException;
import com.innowise.user.mapper.UserMapper;
import com.innowise.user.repository.PaymentCardRepository;
import com.innowise.user.repository.UserRepository;
import com.innowise.user.service.UserService;
import com.innowise.user.specification.UserSpecification;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PaymentCardRepository paymentCardRepository;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PaymentCardRepository paymentCardRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.paymentCardRepository = paymentCardRepository;
    }

    @Override
    @Transactional
    public UserResponseDto createUser(UserRequestDto dto) {
        User user = userMapper.toEntity(dto);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findWithCardsById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.toDto(user);
    }

    @Override
    public Page<UserResponseDto> getUsers(String firstName, String lastName, Pageable pageable) {
        Specification<User> spec = null;

        if (firstName != null && !firstName.isBlank()) {
            spec = UserSpecification.hasFirstName(firstName);
        }

        if (lastName != null && !lastName.isBlank()) {
            spec = (spec == null)
                    ? UserSpecification.hasLastName(lastName)
                    : spec.and(UserSpecification.hasLastName(lastName));
        }

        Page<User> users = userRepository.findAll(spec, pageable);
        return users.map(userMapper::toDto);
    }

    @Override
    @Transactional
    @CachePut(value = "users", key = "#id")
    public UserResponseDto updateUser(Long id, UserRequestDto dto) {
        User existing = getUserEntityById(id);
        existing.setName(dto.getName());
        existing.setSurname(dto.getSurname());
        existing.setEmail(dto.getEmail());
        existing.setBirthDate(dto.getBirthDate());
        return userMapper.toDto(userRepository.save(existing));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#id"),
            @CacheEvict(value = "cardsByUser", key = "#id"),
            @CacheEvict(value = "cards", allEntries = true)
    })
    public void activateUser(Long id) {
        User user = getUserEntityById(id);
        user.setActive(true);
        userRepository.save(user);

        paymentCardRepository.activateCardsByUserId(id);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#id"),
            @CacheEvict(value = "cardsByUser", key = "#id"),
            @CacheEvict(value = "cards", allEntries = true)
    })
    public void deactivateUser(Long id) {
        User user = getUserEntityById(id);
        user.setActive(false);
        userRepository.save(user);

        paymentCardRepository.deactivateCardsByUserId(id);
    }

    @Override
    public Optional<UserResponseDto> getUserByEmail(String email) {
        return userRepository.findByEmail(email).map(userMapper::toDto);
    }

    private User getUserEntityById(Long id) {
        return userRepository.findByIdIncludingInactive(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
