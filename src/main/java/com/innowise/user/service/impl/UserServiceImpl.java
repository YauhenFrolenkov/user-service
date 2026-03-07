package com.innowise.user.service.impl;

import com.innowise.user.entity.User;
import com.innowise.user.repository.UserRepository;
import com.innowise.user.service.UserService;
import com.innowise.user.specification.UserSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public Page<User> getUsers(String firstName, String lastName, Pageable pageable) {
        Specification<User> spec = null;

        if (firstName != null && !firstName.isBlank()) {
            spec = UserSpecification.hasFirstName(firstName);
        }

        if (lastName != null && !lastName.isBlank()) {
            spec = (spec == null)
                    ? UserSpecification.hasLastName(lastName)
                    : spec.and(UserSpecification.hasLastName(lastName));
        }

        return userRepository.findAll(spec, pageable);
    }

    @Override
    public User updateUser(Long id, User user) {
        User existing = getUserById(id);
        existing.setName(user.getName());
        existing.setSurname(user.getSurname());
        existing.setEmail(user.getEmail());
        existing.setBirthDate(user.getBirthDate());
        return userRepository.save(existing);
    }

    @Override
    public void activateUser(Long id) {
        User user = getUserById(id);
        user.setActive(true);
        userRepository.save(user);
    }

    @Override
    public void deactivateUser(Long id) {
        User user = getUserById(id);
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
