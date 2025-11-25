//package com.example.studybot.Service;
//
//import com.example.studybot.Repository.ChatMessageRepository;
//import com.example.studybot.model.ChatMessage;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class OpenAiService {
//
//    private final ChatMessageRepository chatRepo;
//    private final RestTemplate restTemplate;
//
//    @Value("${openai.api.key}")
//    private String openAiKey;
//
//    private final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
//
//    public OpenAiService(ChatMessageRepository chatRepo, RestTemplate restTemplate) {
//        this.chatRepo = chatRepo;
//        this.restTemplate = restTemplate;
//    }
//
//    public ChatMessage sendMessage(String userEmail, String userMessage) {
//        // Save user message
//        ChatMessage userMsg = new ChatMessage();
//        userMsg.setUserEmail(userEmail);
//        userMsg.setMessage(userMessage);
//        userMsg.setFromBot(false);
//        userMsg.setCreatedAt(LocalDateTime.now());
//        chatRepo.save(userMsg);
//
//        // Prepare OpenAI request body
//        Map<String, Object> body = new HashMap<>();
//        body.put("model", "gpt-3.5-turbo");  // Updated model
//        body.put("temperature", 0.7);
//
//        // Chat messages format
//        List<Map<String, String>> messages = List.of(
//                Map.of("role", "user", "content", userMessage)
//        );
//        body.put("messages", messages);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setBearerAuth(openAiKey);
//
//        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
//
//        String aiText;
//        try {
//            ResponseEntity<Map> response = restTemplate.postForEntity(OPENAI_URL, request, Map.class);
//            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
//            aiText = ((Map<String, Object>) choices.get(0).get("message")).get("content").toString();
//        } catch (Exception e) {
//            e.printStackTrace();
//            aiText = "Sorry, I could not process your message.";
//        }
//
//        // Save AI response
//        ChatMessage botMsg = new ChatMessage();
//        botMsg.setUserEmail(userEmail);
//        botMsg.setMessage(aiText);
//        botMsg.setFromBot(true);
//        botMsg.setCreatedAt(LocalDateTime.now());
//        chatRepo.save(botMsg);
//
//        return botMsg;
//    }
//
//    public List<ChatMessage> getChatHistory(String userEmail) {
//        return chatRepo.findByUserEmailOrderByCreatedAtAsc(userEmail);
//    }
//}
