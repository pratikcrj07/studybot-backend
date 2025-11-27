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

    @Value("${groq.api.key1}")
    private String groqApiKey1;

    @Value("${groq.api.key2}")
    private String groqApiKey2;

    @Value("${groq.api.url}")
    private String groqApiUrl;

    @Value("${groq.model}")
    private String groqModel;

    public GroqChatService(ChatMessageRepository chatRepo, RestTemplate restTemplate) {
        this.chatRepo = chatRepo;
        this.restTemplate = restTemplate;
    }

    public ChatMessage sendMessage(String userEmail, String userMessage) {

        ChatMessage userMsg = new ChatMessage();
        userMsg.setUserEmail(userEmail);
        userMsg.setMessage(userMessage);
        userMsg.setFromBot(false);
        userMsg.setCreatedAt(LocalDateTime.now());
        chatRepo.save(userMsg);

        List<ChatMessage> history = chatRepo.findByUserEmailOrderByCreatedAtAsc(userEmail);
        List<Map<String, String>> messagesForAI = prepareMessages(history);

        Map<String, Object> body = new HashMap<>();
        body.put("model", groqModel);
        body.put("messages", messagesForAI);

        String aiResponse = callGroqApi(body);

        ChatMessage botMsg = new ChatMessage();
        botMsg.setUserEmail(userEmail);
        botMsg.setMessage(aiResponse);
        botMsg.setFromBot(true);
        botMsg.setCreatedAt(LocalDateTime.now());
        chatRepo.save(botMsg);

        return botMsg;
    }

    private List<Map<String, String>> prepareMessages(List<ChatMessage> history) {
        List<Map<String, String>> messagesForAI = new ArrayList<>();

        int retainCount = 10;
        int total = history.size();

        if (total > retainCount) {
            StringBuilder summary = new StringBuilder();
            for (int i = 0; i < total - retainCount; i++) {
                ChatMessage msg = history.get(i);
                summary.append(msg.isFromBot() ? "Bot: " : "User: ")
                        .append(msg.getMessage()).append(" ");
            }
            messagesForAI.add(Map.of("role", "system", "content", "Summary of previous conversation: " + summary.toString()));
            history = history.subList(total - retainCount, total); // keep last N
        }

        for (ChatMessage msg : history) {
            messagesForAI.add(Map.of(
                    "role", msg.isFromBot() ? "assistant" : "user",
                    "content", msg.getMessage()
            ));
        }

        return messagesForAI;
    }

    private String callGroqApi(Map<String, Object> body) {
        List<String> apiKeys = List.of(groqApiKey1, groqApiKey2);
        int currentIndex = 0;

        while (currentIndex < apiKeys.size()) {
            String key = apiKeys.get(currentIndex);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(key);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            try {
                ResponseEntity<Map> response = restTemplate.postForEntity(groqApiUrl, request, Map.class);
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                return ((Map<String, Object>) choices.get(0).get("message")).get("content").toString();
            } catch (Exception e) {
                if (e.getMessage().contains("429") || e.getMessage().contains("rate limit")) {
                    System.out.println("Key " + key + " hit rate limit, switching key...");
                    currentIndex++;
                } else {
                    e.printStackTrace();
                    return "Sorry, I could not process your message.";
                }
            }
        }

        return "Sorry, all API keys are currently rate-limited. Try again later.";
    }

    public List<ChatMessage> getChatHistory(String userEmail) {
        return chatRepo.findByUserEmailOrderByCreatedAtAsc(userEmail);
    }
}
