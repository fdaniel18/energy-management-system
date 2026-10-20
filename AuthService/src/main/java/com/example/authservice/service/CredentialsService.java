package com.example.authservice.service;

import com.example.authservice.model.Credentials;
import com.example.authservice.repository.CredentialsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CredentialsService {

    private final CredentialsRepository credentialsRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Credentials registerCredentials(Long userId, String email, String rawPassword) {
        if (credentialsRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email-ul este deja folosit!");
        }

        Credentials credentials = Credentials.builder()
                .userId(userId)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role("USER")
                .build();

        return credentialsRepository.save(credentials);
    }

    public Optional<Credentials> findByEmail(String email) {
        return credentialsRepository.findByEmail(email);
    }


    public boolean validateCredentials(String email, String rawPassword) {
        Optional<Credentials> credentialsOpt = credentialsRepository.findByEmail(email);
        if (credentialsOpt.isEmpty()) {
            return false;
        }

        Credentials credentials = credentialsOpt.get();
        return passwordEncoder.matches(rawPassword, credentials.getPassword());
    }

    public String getRoleByEmail(String email) {
        return credentialsRepository.findByEmail(email)
                .map(Credentials::getRole)
                .orElse("UNKNOWN");
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        credentialsRepository.findByUserId(userId).ifPresent(credentials -> {
            credentialsRepository.delete(credentials);
        });
    }

    @Transactional
    public void updateEmail(Long userId, String newEmail) {
        credentialsRepository.findByUserId(userId).ifPresent(credentials -> {
            credentials.setEmail(newEmail);
            credentialsRepository.save(credentials);
        });
    }
}
