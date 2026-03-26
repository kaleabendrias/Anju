package com.anju.config;

import com.anju.entity.User;
import com.anju.repository.UserRepository;
import com.anju.security.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String DEFAULT_PASSWORD = "Anju@1234";
    private static final int PASSWORD_LENGTH = 16;
    private static final String PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789!@#$%";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Value("${security.force-password-rotation:false}")
    private boolean forcePasswordRotation;

    @Value("${security.seed-default-passwords:true}")
    private boolean seedDefaultPasswords;

    private final AtomicBoolean seeded = new AtomicBoolean(false);

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder, Environment environment) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    private record SeedUser(String username, User.Role role) {}

    @Override
    @Transactional
    public void run(String... args) {
        if (seeded.getAndSet(true)) {
            log.debug("Data seeding already completed, skipping");
            return;
        }

        if (!shouldSeedData()) {
            log.info("Skipping data seeding - active profile is not configured for default user seeding");
            return;
        }

        if (!seedDefaultPasswords) {
            log.info("Data seeding disabled via configuration");
            return;
        }

        log.info("=".repeat(60));
        log.info("DATA SEEDING STARTED (Dev/Docker Profiles)");
        log.info("Force Password Rotation: {}", forcePasswordRotation);
        log.info("=".repeat(60));

        List<SeedUser> seedUsers = List.of(
            new SeedUser("admin", User.Role.ADMIN),
            new SeedUser("reviewer", User.Role.REVIEWER),
            new SeedUser("dispatcher", User.Role.DISPATCHER),
            new SeedUser("finance", User.Role.FINANCE),
            new SeedUser("frontline", User.Role.FRONTLINE)
        );

        for (SeedUser seedUser : seedUsers) {
            seedUserIfNotExists(seedUser);
        }

        log.info("=".repeat(60));
        log.info("DATA SEEDING COMPLETED");
        log.warn("SECURITY WARNING: Default passwords are active in dev profile.");
        log.warn("Use /api/admin/users/{id}/reset-password to set new passwords before production.");
        if (forcePasswordRotation) {
            log.warn("FORCED PASSWORD ROTATION: All seeded accounts require password reset.");
        }
        log.info("=".repeat(60));
    }

    private boolean shouldSeedData() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("dev")
                        || profile.equalsIgnoreCase("development")
                        || profile.equalsIgnoreCase("docker"));
    }

    private void seedUserIfNotExists(SeedUser seedUser) {
        var existingUser = userRepository.findByUsername(seedUser.username());
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            
            if (forcePasswordRotation) {
                String newPassword = generateSecurePassword();
                user.setPasswordHash(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                log.warn("PASSWORD ROTATED for user '{}': New temporary credential generated. User ID: {}", 
                        seedUser.username(), user.getId());
                log.warn("Temporary credential is intentionally not logged. Distribute via secure channel.");
            } else {
                log.debug("User already exists, skipping: {}", seedUser.username());
            }
            return;
        }

        String passwordToUse = forcePasswordRotation ? generateSecurePassword() : DEFAULT_PASSWORD;
        
        User user = User.builder()
            .username(seedUser.username())
            .passwordHash(passwordEncoder.encode(passwordToUse))
            .role(seedUser.role())
            .build();
        userRepository.save(user);
        
        log.info("Seeded user: {} with role: {}", seedUser.username(), seedUser.role());
        
        if (forcePasswordRotation) {
            log.warn("Temporary credential for '{}' was generated and stored securely (not logged).", seedUser.username());
        } else {
            log.warn("Default credential policy active for '{}'. Rotate credentials before production.", seedUser.username());
        }
    }

    private String generateSecurePassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(PASSWORD_CHARS.charAt(RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
        return password.toString();
    }

    public boolean isForcePasswordRotationEnabled() {
        return forcePasswordRotation;
    }

    public boolean isDevProfile() {
        return shouldSeedData();
    }
}
