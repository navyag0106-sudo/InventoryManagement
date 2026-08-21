package com.inventory.controller;

import com.inventory.dto.AuthResponse;
import com.inventory.dto.LoginRequest;
import com.inventory.dto.RegisterRequest;
import com.inventory.dto.UserResponse;
import com.inventory.entity.User;
import com.inventory.security.CustomUserDetails;
import com.inventory.security.JwtUtils;
import com.inventory.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserService userService, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername().trim(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        AuthResponse response = new AuthResponse(
                jwt,
                userDetails.getUserId(),
                userDetails.getUsername(),
                userDetails.getFullName(),
                userDetails.getRole(),
                "Login successful"
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);

        String jwt = jwtUtils.generateTokenFromUsername(
                user.getUsername(),
                user.getUserId(),
                user.getFullName(),
                user.getRole()
        );

        AuthResponse response = new AuthResponse(
                jwt,
                user.getUserId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                "User registered successfully"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            Map<String, Object> body = new HashMap<>();
            body.put("status", 401);
            body.put("message", "Not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }

        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userService.findById(userDetails.getUserId());
            return ResponseEntity.ok(UserResponse.fromEntity(user));
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpSession session) {
        SecurityContextHolder.clearContext();
        if (session != null) {
            try {
                session.invalidate();
            } catch (Exception ignored) {
            }
        }
        Map<String, String> body = new HashMap<>();
        body.put("message", "Logout successful");
        return ResponseEntity.ok(body);
    }

    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> checkSession() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> body = new HashMap<>();

        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            body.put("loggedIn", true);
            body.put("username", authentication.getName());
            if (authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                body.put("userId", userDetails.getUserId());
                body.put("fullName", userDetails.getFullName());
                body.put("role", userDetails.getRole());
            }
        } else {
            body.put("loggedIn", false);
        }
        return ResponseEntity.ok(body);
    }
}
