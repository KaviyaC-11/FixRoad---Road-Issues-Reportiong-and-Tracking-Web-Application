package com.fixroad.service;

import com.fixroad.dto.ForgotPasswordRequest;
import com.fixroad.dto.RegisterRequest;
import com.fixroad.dto.ResetPasswordRequest;
import com.fixroad.model.Role;
import com.fixroad.model.User;
import com.fixroad.model.Otp;
import com.fixroad.repository.UserRepository;
import com.fixroad.security.JwtService;
import com.fixroad.repository.OtpRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

@Service // Marks this class as a Spring Service component
public class AuthService {

    // ========================= DEPENDENCIES =========================

    // Repository for user database operations
    private final UserRepository userRepository;

    // Password encoder for hashing user passwords
    private final BCryptPasswordEncoder passwordEncoder;

    // Repository for storing OTP records
    private final OtpRepository otpRepository;

    // Service for sending emails (OTP / reset links)
    private final EmailService emailService;

    // Service for generating JWT tokens
    private final JwtService jwtService;

    // ========================= CONSTRUCTOR =========================
    // Constructor-based dependency injection
    public AuthService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       OtpRepository otpRepository,
                       JwtService jwtService,
                       EmailService emailService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpRepository = otpRepository;
        this.emailService = emailService;
        this.jwtService = jwtService;
    }

    // ========================= REGISTER =========================
    // Handles user registration and OTP generation
    public void register(RegisterRequest request) {

        // Check if user already exists
        User existingUser = userRepository.findByEmail(request.getEmail()).orElse(null);

        // Case 1: User exists and already verified → block registration
        if (existingUser != null && existingUser.isVerified()) {
            throw new RuntimeException("Email already exists. Please login.");
        }

        // Case 2: User exists but NOT verified → resend OTP
        if (existingUser != null && !existingUser.isVerified()) {

            // Generate new OTP
            String otpCode = String.valueOf(100000 + new Random().nextInt(900000));

            // Create OTP entity
            Otp otp = new Otp();
            otp.setEmail(existingUser.getEmail());
            otp.setOtpCode(otpCode);
            otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));
            otp.setUsed(false);

            // Save OTP in database
            otpRepository.save(otp);

            // Send OTP email
            emailService.sendOtp(existingUser.getEmail(), otpCode);

            return; // Do not create new user
        }

        // Case 3: New user → create account
        User user = new User();

        // Set user details
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setVerified(false);

        // Save new user
        userRepository.save(user);

        // Generate OTP for verification
        String otpCode = String.valueOf(100000 + new Random().nextInt(900000));

        // Create OTP entity
        Otp otp = new Otp();
        otp.setEmail(user.getEmail());
        otp.setOtpCode(otpCode);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otp.setUsed(false);

        // Save OTP
        otpRepository.save(otp);

        // Send OTP email
        emailService.sendOtp(user.getEmail(), otpCode);
    }

    // ========================= VERIFY OTP =========================
    // Verifies OTP and activates user account
    public void verifyOtp(String email, String otpCode) {

        // Get latest OTP for email
        Otp otp = otpRepository
                .findTopByEmailOrderByExpiresAtDesc(email)
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        // Check if OTP already used
        if (otp.isUsed()) {
            throw new RuntimeException("OTP already used");
        }

        // Check if OTP expired
        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        // Validate OTP code
        if (!otp.getOtpCode().equals(otpCode)) {
            throw new RuntimeException("Invalid OTP");
        }

        // Mark OTP as used
        otp.setUsed(true);
        otpRepository.save(otp);

        // Fetch user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Mark user as verified
        user.setVerified(true);
        userRepository.save(user);
    }

    // ========================= LOGIN =========================
    // Authenticates user and returns JWT token
    public Map<String, String> login(String email, String password) {

        // Check if user exists
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Validate password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Check if email is verified
        if (!user.isVerified()) {
            throw new RuntimeException("Please verify your email before logging in.");
        }

        // Generate JWT token
        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        // Return authentication response
        return Map.of(
                "token", token,
                "role", user.getRole().name(),
                "name", user.getName()
        );
    }

    // ========================= FORGOT PASSWORD =========================
    // Handles forgot password request and sends reset link
    public void forgotPassword(ForgotPasswordRequest request) {

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate password reset token
        String token = UUID.randomUUID().toString();

        // Save reset token and expiry time
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);

        // Send reset email
        emailService.sendEmail(user.getEmail(), token);
    }

    // ========================= RESET PASSWORD =========================
    // Resets password using reset token
    public void resetPassword(ResetPasswordRequest request) {

        // Find user by reset token
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        // Check if reset token expired
        if (user.getResetTokenExpiry() == null ||
                user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {

            throw new RuntimeException("Token expired");
        }

        // Encode and update new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // Clear reset token
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        // Save updated user
        userRepository.save(user);
    }
}