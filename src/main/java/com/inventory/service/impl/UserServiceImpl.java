package com.inventory.service.impl;

import com.inventory.dto.RegisterRequest;
import com.inventory.dto.UserUpdateRequest;
import com.inventory.entity.User;
import com.inventory.exception.DuplicateResourceException;
import com.inventory.exception.UserNotFoundException;
import com.inventory.repository.UserRepository;
import com.inventory.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User register(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new DuplicateResourceException("Username already exists: " + user.getUsername());
        }
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (!StringUtils.hasText(user.getRole())) {
            user.setRole("USER");
        }
        if (!StringUtils.hasText(user.getStatus())) {
            user.setStatus("ACTIVE");
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername().trim()).isPresent()) {
            throw new DuplicateResourceException("Username already exists: " + request.getUsername().trim());
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName() != null ? request.getFullName().trim() : null);

        String role = StringUtils.hasText(request.getRole()) ? request.getRole().trim().toUpperCase() : "USER";
        if (role.startsWith("ROLE_")) {
            role = role.substring(5);
        }
        if (!"ADMIN".equals(role) && !"USER".equals(role)) {
            role = "USER";
        }
        user.setRole(role);
        user.setStatus("ACTIVE");

        return userRepository.save(user);
    }

    @Override
    public Optional<User> login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if ("ACTIVE".equalsIgnoreCase(user.getStatus())) {
                if (passwordEncoder.matches(password, user.getPassword()) || user.getPassword().equals(password)) {
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User updateUser(Long id, UserUpdateRequest request) {
        User user = findById(id);

        if (StringUtils.hasText(request.getFullName())) {
            user.setFullName(request.getFullName().trim());
        }
        if (StringUtils.hasText(request.getRole())) {
            String role = request.getRole().trim().toUpperCase();
            if (role.startsWith("ROLE_")) role = role.substring(5);
            if ("ADMIN".equals(role) || "USER".equals(role)) {
                user.setRole(role);
            }
        }
        if (StringUtils.hasText(request.getStatus())) {
            String status = request.getStatus().trim().toUpperCase();
            if ("ACTIVE".equals(status) || "INACTIVE".equals(status)) {
                user.setStatus(status);
            }
        }
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = findById(id);
        userRepository.delete(user);
    }
}
