package com.demo.auth.service;

import com.demo.auth.models.entities.OtpEntity;
import com.demo.auth.repositories.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final EmailNotificationService emailNotificationService;
    private final OtpRepository otpRepository;

    private static final int OTP_EXPIRY_MINUTES = 10;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void generateAndSendOtp(String toEmail) {
        // Delete any existing OTP for this email
        otpRepository.deleteByEmail(toEmail);

        // Generate 6-digit OTP
        String otpCode = String.format("%06d", secureRandom.nextInt(1000000));

        // Save to Database
        OtpEntity otpEntity = OtpEntity.builder()
                .email(toEmail)
                .otpCode(otpCode)
                .expiryTime(Instant.now().plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES))
                .build();
        otpRepository.save(otpEntity);

        // Send Email Asynchronously via another bean to allow proxy creation
        emailNotificationService.sendOtpEmail(toEmail, otpCode, OTP_EXPIRY_MINUTES);
    }

    @Transactional
    public boolean verifyOtp(String email, String otpCode) {
        Optional<OtpEntity> optOtp = otpRepository.findByEmail(email);

        if (optOtp.isEmpty()) {
            return false;
        }

        OtpEntity otpEntity = optOtp.get();

        // Check Expiry
        if (Instant.now().isAfter(otpEntity.getExpiryTime())) {
            otpRepository.delete(otpEntity);
            return false;
        }

        // Check Match
        if (otpEntity.getOtpCode().equals(otpCode)) {
            otpRepository.delete(otpEntity);
            return true;
        }

        return false;
    }
}
