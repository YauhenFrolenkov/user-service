package com.innowise.user.service;

import com.innowise.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserService {

    User createUser(User user);

    User getUserById(Long id);

    Page<User> getUsers(String firstName, String lastName, Pageable pageable);

    User updateUser(Long id, User user);

    void activateUser(Long id);

    void deactivateUser(Long id);

    Optional<User> getUserByEmail(String email);

}
