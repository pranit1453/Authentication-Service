package com.demo.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@demo.com}")
    private String fromEmail;

    @Async("taskExecutor")
    public void sendAccountLockedEmail(String toEmail) {
        log.info("Sending Account Locked email to: {}", toEmail);
        String subject = "Security Alert: Your Account has been Locked";
        String content = buildHtmlMessage("Account Locked",
                "Your account has been locked by an Administrator or due to too many failed login attempts. Please contact support if you believe this is an error.");
        sendEmail(toEmail, subject, content);
    }

    @Async("taskExecutor")
    public void sendAccountUnlockedEmail(String toEmail) {
        log.info("Sending Account Unlocked email to: {}", toEmail);
        String subject = "Good News: Your Account has been Unlocked";
        String content = buildHtmlMessage("Account Unlocked",
                "Good news! Your account has been unlocked. You may now sign in using your regular credentials.");
        sendEmail(toEmail, subject, content);
    }

    @Async("taskExecutor")
    public void sendOtpEmail(String toEmail, String otpCode, int expiryMinutes) {
        log.info("Sending OTP email to: {}", toEmail);
        String subject = "Your Security Verification Code";
        String content = "<div style='font-family: Arial, sans-serif; padding: 20px; color: #333;'>" +
                "<h2>Email Verification</h2>" +
                "<p>Thank you for registering. Please use the following One-Time Password (OTP) to verify your email address. "
                +
                "This code is valid for <strong>" + expiryMinutes + " minutes</strong>.</p>" +
                "<div style='background-color: #f4f4f4; padding: 15px; border-radius: 5px; font-size: 24px; font-weight: bold; letter-spacing: 5px; text-align: center; color: #0056b3; margin: 20px 0;'>"
                +
                otpCode +
                "</div>" +
                "<p>If you did not request this verification, please safely ignore this email.</p>" +
                "</div>";
        sendEmail(toEmail, subject, content);
    }

    private void sendEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Notification email sent to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send notification email to {}", toEmail, e);
        }
    }

    private String buildHtmlMessage(String title, String body) {
        return "<div style='font-family: Arial, sans-serif; padding: 20px; color: #333; max-width: 600px; margin: auto; border: 1px solid #ddd;'>"
                +
                "<h2 style='color: #0056b3;'>" + title + "</h2>" +
                "<p style='font-size: 16px; line-height: 1.5;'>" + body + "</p>" +
                "<hr style='border-top: 1px solid #eee; margin: 20px 0;'>" +
                "<p style='font-size: 12px; color: #999;'>This is an automated security notification. Please do not reply.</p>"
                +
                "</div>";
    }
}
