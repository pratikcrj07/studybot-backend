package com.example.studybot.Service;

import com.example.studybot.Repository.UserRepository;
import com.example.studybot.config.JwtUtil;
import com.example.studybot.model.User;
import jakarta.mail.MessagingException; // Required for handling email errors
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JavaMailSender mailSender,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.jwtUtil = jwtUtil;
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

        // Call the new HTML email method
        sendOtpEmail(savedUser);

        return savedUser;
    }

    private String generateOtp() {
        return String.valueOf(100000 + (int)(Math.random() * 900000));
    }

    // --- THIS IS THE UPDATED METHOD ---
    private void sendOtpEmail(User user) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // 'true' indicates this is a multipart message (HTML compatible)
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(user.getEmail());
            helper.setSubject("StudyBot OTP Verification");

            String htmlContent = String.format(
                    "<html>" +
                            "<body style='font-family: Arial, sans-serif; color: #333;'>" +
                            "  <p>Hi <b>%s</b>,</p>" +
                            "  <p>You need to verify your OTP to get access to StudyBot with integration of Groq.</p>" +
                            "  <p>Your OTP is:</p>" +
                            "  <h2 style='color: #007bff; font-weight: bold; letter-spacing: 2px;'>%s</h2>" + // Blue OTP
                            "  <br>" +
                            "  <hr style='border:none; border-top:1px solid #eee;' />" +
                            "  <h1 style='color: #2c3e50; font-size: 30px; margin-top: 10px;'>Bot-Api</h1>" + // Large Footer
                            "</body>" +
                            "</html>",
                    user.getUsername(),
                    user.getVerificationOtp()
            );

            // Set content to HTML
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            // Log the error or handle it as needed
            throw new RuntimeException("Failed to send OTP email", e);
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