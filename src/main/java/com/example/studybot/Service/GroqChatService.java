package com.example.studybot.Service;

import com.example.studybot.Repository.ChatMessageRepository;
import com.example.studybot.model.ChatMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class GroqChatService {

    private final ChatMessageRepository chatRepo;
    private final RestTemplate restTemplate;

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.api.url}")
    private String groqApiUrl;

    @Value("${groq.model}")
    private String groqModel;

    public GroqChatService(ChatMessageRepository chatRepo, RestTemplate restTemplate) {
        this.chatRepo = chatRepo;
        this.restTemplate = restTemplate;
    }

    public ChatMessage sendMessage(String userEmail, String userMessage) {
        // Save user message
        ChatMessage userMsg = new ChatMessage();
        userMsg.setUserEmail(userEmail);
        userMsg.setMessage(userMessage);
        userMsg.setFromBot(false);
        userMsg.setCreatedAt(LocalDateTime.now());
        chatRepo.save(userMsg);


        List<ChatMessage> history = chatRepo.findByUserEmailOrderByCreatedAtAsc(userEmail);
        List<Map<String, String>> messagesForAI = new ArrayList<>();
        for (ChatMessage msg : history) {
            messagesForAI.add(Map.of(
                    "role", msg.isFromBot() ? "assistant" : "user",
                    "content", msg.getMessage()
            ));
        }

        Map<String, Object> body = new HashMap<>();
        body.put("model", groqModel);
        body.put("messages", messagesForAI);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        String aiResponse;
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(groqApiUrl, request, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            aiResponse = ((Map<String, Object>) choices.get(0).get("message")).get("content").toString();
        } catch (Exception e) {
            e.printStackTrace();
            aiResponse = "Sorry, I could not process your message.";
        }

        // Save bot response
        ChatMessage botMsg = new ChatMessage();
        botMsg.setUserEmail(userEmail);
        botMsg.setMessage(aiResponse);
        botMsg.setFromBot(true);
        botMsg.setCreatedAt(LocalDateTime.now());
        chatRepo.save(botMsg);

        return botMsg;
    }

    // THIS METHOD IS REQUIRED FOR THE CONTROLLER
    public List<ChatMessage> getChatHistory(String userEmail) {
        return chatRepo.findByUserEmailOrderByCreatedAtAsc(userEmail);
    }
}
