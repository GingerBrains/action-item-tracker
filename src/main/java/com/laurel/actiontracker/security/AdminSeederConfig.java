package com.laurel.actiontracker.security;

import com.laurel.actiontracker.entity.User;
import com.laurel.actiontracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminSeederConfig {

    @Value("${app.admin.email}")
    private String seedEmail;

    @Value("${app.admin.fullName}")
    private String fullName;

    @Value("${app.admin.password}")
    private String seedPassword;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminSeederConfig(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public CommandLineRunner seedAdmin() {
        return args -> {
            boolean adminExists = userRepository.existsByRole(User.Role.ADMIN);

            if (adminExists) {
                return;
            }

            User adminUser = new User();
            adminUser.setEmail(seedEmail);
            adminUser.setFullName(fullName);
            adminUser.setRole(User.Role.ADMIN);
            adminUser.setPasswordHash(passwordEncoder.encode(seedPassword));

            userRepository.save(adminUser);
        };
    }
}
