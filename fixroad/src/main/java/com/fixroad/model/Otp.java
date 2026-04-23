package com.fixroad.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Otp {

    @Id
    @GeneratedValue
    private UUID id;

    private String email;

    private String otpCode;

    private LocalDateTime expiresAt;

    private boolean used;

    // Getters & Setters

    public UUID getId() { 
        return id; 
    }

    public void setId(UUID id) { 
        this.id = id; 
    }

    public String getEmail() { 
        return email; 
    }

    public void setEmail(String email) { 
        this.email = email; 
    }

    public String getOtpCode() { 
        return otpCode; 
    }

    public void setOtpCode(String otpCode) {
         this.otpCode = otpCode; 
    }

    public LocalDateTime getExpiresAt() { 
        return expiresAt; 
    }

    public void setExpiresAt(LocalDateTime expiresAt) { 
        this.expiresAt = expiresAt; 
    }

    public boolean isUsed() { 
        return used; 
    }

    public void setUsed(boolean used) { 
        this.used = used; 
    }

}