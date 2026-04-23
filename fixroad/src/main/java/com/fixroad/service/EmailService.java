package com.fixroad.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service // Marks this class as a Spring Service component
public class EmailService {

    // Dependency to send emails using Spring Mail
    private final JavaMailSender mailSender;

    // Constructor-based dependency injection
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ========================= OTP EMAIL =========================
    // Sends OTP for email verification
    public void sendOtp(String toEmail, String otp) {

        // Create email message object
        SimpleMailMessage message = new SimpleMailMessage();

        // Set recipient email
        message.setTo(toEmail);

        // Set subject of email
        message.setSubject("FixRoad – Email Verification Code");

        // Set email body content
        message.setText(
                "Dear User,\n\n"
                        + "Thank you for registering with FixRoad.\n\n"
                        + "To complete your account verification, please use the One-Time Password (OTP) provided below:\n\n"
                        + "🔐 Your Verification Code: " + otp + "\n\n"
                        + "This code is valid for 5 minutes. For security reasons, please do not share this code with anyone.\n\n"
                        + "If you did not request this verification, please ignore this email.\n\n"
                        + "Thank you,\n"
                        + "FixRoad Support Team\n"
                        + "Road Issue Reporting & Tracking System"
        );

        // Send email
        mailSender.send(message);
    }

    // ========================= PASSWORD RESET EMAIL =========================
    // Sends password reset link with token
    public void sendEmail(String toEmail, String token) {

        // Email subject
        String subject = "FixRoad – Password Reset Request";

        // Generate reset link with token
        String resetLink = "http://localhost:5500/reset-password.html?token=" + token;

        // Email body content
        String body =
                "Dear User,\n\n"
                        + "We received a request to reset your FixRoad account password.\n\n"
                        + "Please click the link below to reset your password:\n\n"
                        + resetLink + "\n\n"
                        + "This link will expire in 15 minutes for security reasons.\n\n"
                        + "If you did not request a password reset, please ignore this email.\n\n"
                        + "For your security, do not share this link with anyone.\n\n"
                        + "Thank you,\n"
                        + "FixRoad Support Team\n"
                        + "Road Issue Reporting & Tracking System";

        // Create email message
        SimpleMailMessage message = new SimpleMailMessage();

        // Set recipient
        message.setTo(toEmail);

        // Set subject
        message.setSubject(subject);

        // Set body
        message.setText(body);

        // Send email
        mailSender.send(message);
    }

    // ========================= REPORTED EMAIL =========================
    public void sendIssueReportedEmail(String toEmail, String issueTitle, String place) {

    String subject = "FixRoad – Issue Reported Successfully";

    String body =
            "Dear Citizen,\n\n"
                    + "Your issue has been successfully reported.\n\n"
                    + "Issue: " + issueTitle + "\n"
                    + "Location: " + place + "\n\n"
                    + "Our team will review it shortly.\n\n"
                    + "Thank you,\n"
                    + "FixRoad Support Team";

    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(toEmail);
    message.setSubject(subject);
    message.setText(body);

    mailSender.send(message);
}

    // ========================= REPAIR ASSIGNED EMAIL =========================
    // Notifies user when a repair team is assigned
    public void sendRepairAssignedEmail(String toEmail, String issueTitle,
                                        String place, String teamName, String contact) {

        // Email subject
        String subject = "FixRoad – Repair Team Assigned";

        // Email body content
        String body =
                "Dear Citizen,\n\n"
                        + "A repair team has been assigned to your reported issue.\n\n"
                        + "Issue: " + issueTitle + "\n"
                        + "Location: " + place + "\n\n"
                        + "Repair Team: " + teamName + "\n"
                        + "Contact: " + contact + "\n\n"
                        + "Our team will begin repair work shortly.\n\n"
                        + "Thank you for helping improve your community.\n\n"
                        + "FixRoad Support Team";

        // Create email message
        SimpleMailMessage message = new SimpleMailMessage();

        // Set recipient
        message.setTo(toEmail);

        // Set subject
        message.setSubject(subject);

        // Set body
        message.setText(body);

        // Send email
        mailSender.send(message);
    }

    // ========================= ISSUE RESOLVED EMAIL =========================
    // Notifies user when issue is resolved
    public void sendIssueResolvedEmail(String toEmail, String issueTitle, String place) {

        // Email subject
        String subject = "FixRoad – Issue Resolved";

        // Email body content
        String body =
                "Dear Citizen,\n\n"
                        + "Good news! Your reported issue has been resolved.\n\n"
                        + "Issue: " + issueTitle + "\n"
                        + "Location: " + place + "\n\n"
                        + "Thank you for helping improve road safety in your community.\n\n"
                        + "FixRoad Support Team";

        // Create email message
        SimpleMailMessage message = new SimpleMailMessage();

        // Set recipient
        message.setTo(toEmail);

        // Set subject
        message.setSubject(subject);

        // Set body
        message.setText(body);

        // Send email
        mailSender.send(message);
    }

    // ========================= REPAIR UPDATED EMAIL =========================
    // Notifies user when repair team details are updated
    public void sendRepairUpdatedEmail(String toEmail,
                                       String issueTitle,
                                       String place,
                                       String oldTeam,
                                       String oldContact,
                                       String newTeam,
                                       String newContact) {

        // Email subject
        String subject = "FixRoad – Repair Team Updated";

        // Email body content
        String body =
                "Dear Citizen,\n\n"
                        + "The repair team assigned to your reported issue has been changed.\n\n"
                        + "Issue: " + issueTitle + "\n"
                        + "Location: " + place + "\n\n"
                        + "Previous Team: " + oldTeam + "\n"
                        + "Previous Contact: " + oldContact + "\n\n"
                        + "Updated Team: " + newTeam + "\n"
                        + "Updated Contact: " + newContact + "\n\n"
                        + "FixRoad Support Team";

        // Create email message
        SimpleMailMessage message = new SimpleMailMessage();

        // Set recipient
        message.setTo(toEmail);

        // Set subject
        message.setSubject(subject);

        // Set body
        message.setText(body);

        // Send email
        mailSender.send(message);
    }
}