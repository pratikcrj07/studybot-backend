package com.example.studybot.Service;

import com.example.studybot.Repository.ChatMessageRepository;
import com.example.studybot.model.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class GroqChatService {

    private static final Logger logger = LoggerFactory.getLogger(GroqChatService.class);

    private final ChatMessageRepository chatRepo;
    private final RestTemplate restTemplate;

    private final String groqApiUrl = "https://api.groq.ai/v1/chat/completions"; // adjust if needed
    private final String groqModel = "gpt-3.5"; // adjust your model

    public GroqChatService(ChatMessageRepository chatRepo, RestTemplate restTemplate) {
        this.chatRepo = chatRepo;
        this.restTemplate = restTemplate;
    }

    // Sends a message and returns bot response
    public ChatMessage sendMessage(String userEmail, String userMessage, String jwtToken) {

        // Save user message
        ChatMessage userMsg = new ChatMessage();
        userMsg.setUserEmail(userEmail);
        userMsg.setMessage(userMessage);
        userMsg.setFromBot(false);
        userMsg.setCreatedAt(LocalDateTime.now());
        chatRepo.save(userMsg);

        // Prepare message history for AI
        List<ChatMessage> history = chatRepo.findByUserEmailOrderByCreatedAtAsc(userEmail);
        List<Map<String, String>> messagesForAI = prepareMessages(history);

        // Build request body
        Map<String, Object> body = new HashMap<>();
        body.put("model", groqModel);
        body.put("messages", messagesForAI);

        // Call Groq API
        String aiResponse = callGroqApi(body, jwtToken);

        // Save bot response
        ChatMessage botMsg = new ChatMessage();
        botMsg.setUserEmail(userEmail);
        botMsg.setMessage(aiResponse);
        botMsg.setFromBot(true);
        botMsg.setCreatedAt(LocalDateTime.now());
        chatRepo.save(botMsg);

        return botMsg;
    }

    // Prepares the last N messages + optional summary for AI
    private List<Map<String, String>> prepareMessages(List<ChatMessage> history) {
        List<Map<String, String>> messagesForAI = new ArrayList<>();
        int retainCount = 10;
        int total = history.size();

        // Add summary if history is large
        if (total > retainCount) {
            StringBuilder summary = new StringBuilder();
            for (int i = 0; i < total - retainCount; i++) {
                ChatMessage msg = history.get(i);
                summary.append(msg.isFromBot() ? "Bot: " : "User: ")
                        .append(msg.getMessage()).append(" ");
            }
            messagesForAI.add(Map.of(
                    "role", "system",
                    "content", "Summary of previous conversation: " + summary.toString()
            ));
            history = history.subList(total - retainCount, total);
        }

        // Add recent messages
        for (ChatMessage msg : history) {
            messagesForAI.add(Map.of(
                    "role", msg.isFromBot() ? "assistant" : "user",
                    "content", msg.getMessage()
            ));
        }

        return messagesForAI;
    }

    // Calls the Groq API using JWT for Bearer auth
    private String callGroqApi(Map<String, Object> body, String jwtToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(groqApiUrl, request, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody == null || !responseBody.containsKey("choices")) {
                logger.warn("No 'choices' in Groq response");
                return "Sorry, no response from AI.";
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (choices.isEmpty()) {
                return "AI returned empty response.";
            }

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            if (message == null || message.get("content") == null) {
                return "AI returned empty message.";
            }

            return message.get("content").toString();

        } catch (HttpClientErrorException e) {
            logger.error("HTTP error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "Error from AI service: " + e.getStatusCode();
        } catch (ResourceAccessException e) {
            logger.error("Connection error: {}", e.getMessage());
            return "Error connecting to AI service. Try again later.";
        } catch (Exception e) {
            logger.error("Unexpected error calling Groq API", e);
            return "Sorry, something went wrong.";
        }
    }

    // Returns full chat history
    public List<ChatMessage> getChatHistory(String userEmail) {
        return chatRepo.findByUserEmailOrderByCreatedAtAsc(userEmail);
    }
}
