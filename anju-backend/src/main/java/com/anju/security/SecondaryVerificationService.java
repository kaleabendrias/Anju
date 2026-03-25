package com.anju.security;

import com.anju.entity.User;
import com.anju.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SecondaryVerificationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SecondaryVerificationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean verifySecondaryPassword(String username, String password) {
        if (username == null || password == null || password.isEmpty()) {
            return false;
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return passwordEncoder.matches(password, user.getPasswordHash());
    }

    public boolean verifySecondaryPasswordForUser(UserDetails user, String password) {
        if (user == null || password == null || password.isEmpty()) {
            return false;
        }
        return verifySecondaryPassword(user.getUsername(), password);
    }

    public void validateSecondaryPassword(UserDetails user, String password) {
        if (!verifySecondaryPasswordForUser(user, password)) {
            throw new SecondaryVerificationException("Secondary password verification failed");
        }
    }

    public static class SecondaryVerificationException extends RuntimeException {
        public SecondaryVerificationException(String message) {
            super(message);
        }
    }
}
