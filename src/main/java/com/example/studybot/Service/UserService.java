package com.example.studybot.Service;

import com.example.studybot.Repository.UserRepository;
import com.example.studybot.config.JwtUtil;
import com.example.studybot.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;

    @Value("${BREVO_API_KEY}")
    private String brevoApiKey;

    @Value("${BREVO_SENDER_EMAIL}")
    private String brevoSenderEmail;

    @Value("${BREVO_SENDER_NAME}")
    private String brevoSenderName;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.restTemplate = restTemplate;
    }

    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        String otp = generateOtp();
        user.setVerificationOtp(otp);
        user.setEnabled(false);

        User savedUser = userRepository.save(user);

        // Send OTP via Brevo API
        sendOtpEmail(savedUser);

        return savedUser;
    }

    private String generateOtp() {
        return String.valueOf(100000 + (int)(Math.random() * 900000));
    }

    private void sendOtpEmail(User user) {
        String url = "https://api.brevo.com/v3/smtp/email";

        Map<String, Object> body = new HashMap<>();
        body.put("sender", Map.of("name", brevoSenderName, "email", brevoSenderEmail));
        body.put("to", List.of(Map.of("email", user.getEmail(), "name", user.getUsername())));
        body.put("subject", "StudyBot OTP Verification");
        body.put("htmlContent", String.format(
                "<html><body style='font-family: Arial, sans-serif; color: #333;'>" +
                        "<p>Hi <b>%s</b>,</p>" +
                        "<p>You need to verify your OTP to get access to StudyBot.</p>" +
                        "<p>Your OTP is:</p>" +
                        "<h2 style='color: #007bff; font-weight: bold; letter-spacing: 2px;'>%s</h2>" +
                        "<br><hr style='border:none; border-top:1px solid #eee;' />" +
                        "<h1 style='color: #2c3e50; font-size: 30px; margin-top: 10px;'>Bot-Api</h1>" +
                        "</body></html>", user.getUsername(), user.getVerificationOtp()
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, request, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP email via Brevo API", e);
        }
    }

    public void verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not registered"));
        if (user.getVerificationOtp().equals(otp)) {
            user.setEnabled(true);
            user.setVerificationOtp(null);
            userRepository.save(user);
        } else {
            throw new RuntimeException("Invalid OTP");
        }
    }

    public String login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!user.isEnabled()) throw new RuntimeException("Email not verified.");

        if (!passwordEncoder.matches(rawPassword, user.getPassword()))
            throw new RuntimeException("Invalid email or password");

        return jwtUtil.generateToken(user.getEmail());
    }

    public String getEmailFromToken(String token) {
        return jwtUtil.extractEmail(token);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
