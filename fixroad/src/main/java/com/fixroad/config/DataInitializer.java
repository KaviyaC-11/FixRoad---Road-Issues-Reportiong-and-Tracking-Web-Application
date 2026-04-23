package com.fixroad.config;

import com.fixroad.model.Role;
import com.fixroad.model.User;
import com.fixroad.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        if (!userRepository.existsByEmail("authority@fixroad.com")) {

            User authority = new User();
            authority.setName("Authority");
            authority.setEmail("authority@fixroad.com");
            authority.setPassword(passwordEncoder.encode("authFR@123"));
            authority.setRole(Role.AUTHORITY);
            authority.setVerified(true);


            userRepository.save(authority);
        }
    }
}