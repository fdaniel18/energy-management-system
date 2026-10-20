package com.example.authservice.repository;

import com.example.authservice.model.Credentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CredentialsRepository extends JpaRepository<Credentials, Long> {

    Optional<Credentials> findByUserId(Long userId);

    Optional<Credentials> findByEmail(String email);

    boolean existsByEmail(String email);
}
