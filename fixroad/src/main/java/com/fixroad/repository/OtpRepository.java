package com.fixroad.repository;

import com.fixroad.model.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OtpRepository extends JpaRepository<Otp, UUID> {

    Optional<Otp> findTopByEmailOrderByExpiresAtDesc(String email);
}