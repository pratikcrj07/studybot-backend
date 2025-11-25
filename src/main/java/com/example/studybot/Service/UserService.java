package com.example.studybot.Service;

import com.example.studybot.Repository.UserRepository;
import com.example.studybot.config.JwtUtil;
import com.example.studybot.model.User;

import org.springframework.mail.SimpleMailMessage;
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
        sendOtpEmail(savedUser);

        return savedUser;
    }

    private String generateOtp() {
        return String.valueOf(100000 + (int)(Math.random() * 900000));
    }

    private void sendOtpEmail(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("StudyBot OTP Verification");
        message.setText("Hi " + user.getUsername() + "" +
                "\"<!DOCTYPE html>\" +\n" +
                "\n\nYou need to verify your OTP to get acess to Studybot with intregration of Groq,\n\nYour OTP is: " + user.getVerificationOtp());
        mailSender.send(message);
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
