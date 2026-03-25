package com.anju.config;

import com.anju.entity.User;
import com.anju.repository.UserRepository;
import com.anju.security.PasswordEncoder;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(1)
// @RequiredArgsConstructor
// @Slf4j
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private static final String DEFAULT_PASSWORD = "Anju@1234";

    private record SeedUser(String username, User.Role role) {}

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting data seeding...");

        List<SeedUser> seedUsers = List.of(
            new SeedUser("admin", User.Role.ADMIN),
            new SeedUser("dispatcher", User.Role.DISPATCHER),
            new SeedUser("finance", User.Role.FINANCE),
            new SeedUser("frontline", User.Role.FRONTLINE)
        );

        for (SeedUser seedUser : seedUsers) {
            if (!userRepository.existsByUsername(seedUser.username())) {
                User user = User.builder()
                    .username(seedUser.username())
                    .passwordHash(passwordEncoder.encode(DEFAULT_PASSWORD))
                    .role(seedUser.role())
                    .build();
                userRepository.save(user);
                log.info("Seeded user: {} with role: {}", seedUser.username(), seedUser.role());
            } else {
                log.debug("User already exists, skipping: {}", seedUser.username());
            }
        }

        log.info("Data seeding completed.");
    }
}
