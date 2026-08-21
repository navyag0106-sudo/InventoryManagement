package com.inventory.service;

import com.inventory.dto.RegisterRequest;
import com.inventory.dto.UserUpdateRequest;
import com.inventory.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User register(User user);
    User register(RegisterRequest request);
    Optional<User> login(String username, String password);
    User findByUsername(String username);
    User findById(Long id);
    List<User> getAllUsers();
    User updateUser(Long id, UserUpdateRequest request);
    void deleteUser(Long id);
}
