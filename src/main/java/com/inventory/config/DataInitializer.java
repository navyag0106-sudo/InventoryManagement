package com.inventory.config;

import com.inventory.entity.User;
import com.inventory.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Ensure default Admin user exists and has valid BCrypt-hashed password
        Optional<User> adminOpt = userRepository.findByUsername("admin");
        User admin;
        if (!adminOpt.isPresent()) {
            admin = new User();
            admin.setUsername("admin");
            admin.setFullName("System Administrator");
            admin.setStatus("ACTIVE");
        } else {
            admin = adminOpt.get();
        }
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole("ADMIN");
        userRepository.save(admin);
        logger.info("Guaranteed valid ADMIN user: admin / admin123");

        // Ensure default Staff user exists
        Optional<User> userOpt = userRepository.findByUsername("user");
        User staff;
        if (!userOpt.isPresent()) {
            staff = new User();
            staff.setUsername("user");
            staff.setFullName("Staff Operator");
            staff.setStatus("ACTIVE");
        } else {
            staff = userOpt.get();
        }
        staff.setPassword(passwordEncoder.encode("user123"));
        staff.setRole("USER");
        userRepository.save(staff);
        logger.info("Guaranteed valid STAFF user: user / user123");
    }
}
