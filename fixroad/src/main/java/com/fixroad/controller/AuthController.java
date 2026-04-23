package com.fixroad.controller;

import com.fixroad.dto.ForgotPasswordRequest;
import com.fixroad.dto.LoginRequest;
import com.fixroad.dto.RegisterRequest;
import com.fixroad.dto.ResetPasswordRequest;
import com.fixroad.dto.VerifyOtpRequest;
import com.fixroad.service.AuthService;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    // ✅ Manual Constructor
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ==================== REGISTER API ====================
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {

        try {
            authService.register(request);
            return ResponseEntity.ok("Registered successfully. OTP sent to email.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== VERIFY OTP API ====================
    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody VerifyOtpRequest request) {

        try {
            authService.verifyOtp(request.getEmail(), request.getOtp());
            return ResponseEntity.ok("OTP verified successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== LOGIN API ====================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        try {
            Map<String, String> response =
                    authService.login(request.getEmail(), request.getPassword());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== FORGOT PASSWORD API ====================
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request);

        return ResponseEntity.ok("Reset password token sent");
    }

    // ==================== RESET PASSWORD API ====================
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request);

        return ResponseEntity.ok("Password reset successful");
    }
}