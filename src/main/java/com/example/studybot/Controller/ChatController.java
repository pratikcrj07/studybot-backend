
package com.example.studybot.Controller;

import com.example.studybot.Service.GroqChatService;
import com.example.studybot.Service.UserService;
import com.example.studybot.model.ChatMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final GroqChatService groqChatService;
    private final UserService userService;

    public ChatController(GroqChatService groqChatService, UserService userService) {
        this.groqChatService = groqChatService;
        this.userService = userService;
    }

    public static class ChatRequest {
        private String message;
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    @PostMapping("/message")
    public ResponseEntity<?> sendMessage(@RequestHeader("Authorization") String authHeader,
                                         @RequestBody ChatRequest chatRequest) {
        try {
            String token = authHeader.substring(7);
            String email = userService.getEmailFromToken(token);
            ChatMessage botResponse = groqChatService.sendMessage(email, chatRequest.getMessage());
            return ResponseEntity.ok(botResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String email = userService.getEmailFromToken(token);
            List<ChatMessage> history = groqChatService.getChatHistory(email);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
