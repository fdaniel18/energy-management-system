package com.example.websocketmicroservice.controller;

import com.example.websocketmicroservice.dto.ChatMessageDTO;
import com.example.websocketmicroservice.service.OllamaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final OllamaService ollamaService;
    private static final Map<String, String> FAQ = new HashMap<>();

    static {
        FAQ.put("how do i reset my password?", "You can reset your password in the settings page under 'Security'.");
        FAQ.put("how do i add a device?", "Navigate to the 'My Devices' page and click the 'Add Device' button.");
        FAQ.put("what is the maximum consumption limit?", "The limit is set per device. You can view it in the device details.");
        FAQ.put("how do i delete a device?", "Go to 'My Devices', select the device, and click 'Delete'.");
        FAQ.put("can i change my email?", "Yes, you can update your email in the 'Profile' section.");
        FAQ.put("where can i see my consumption history?", "Visit the 'Energy Consumption' page to view charts and history.");
        FAQ.put("how do i contact support?", "You can email us at support@example.com or call +1-555-0199.");
        FAQ.put("is there a mobile app?", "Currently, we only have a web application. A mobile app is coming soon!");
        FAQ.put("how often is data updated?", "Energy consumption data is updated hourly.");
        FAQ.put("what happens if i exceed the limit?", "You will receive a real-time notification alerting you of the overconsumption.");
    }

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessageDTO chatMessage) {
        log.info("Received chat message from user {}: {}", chatMessage.getUserId(), chatMessage.getContent());

        String question = chatMessage.getContent().trim().toLowerCase();
        String answer = FAQ.get(question);

        if (answer == null) {
            // If not in FAQ, ask Ollama
            answer = ollamaService.getAnswer(chatMessage.getContent());
        }

        // Simulate typing delay for better UX
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        ChatMessageDTO response = new ChatMessageDTO("SYSTEM", answer, chatMessage.getUserId());

        // Send response back to the specific user
        String destination = "/topic/chat/" + chatMessage.getUserId();
        messagingTemplate.convertAndSend(destination, response);
        log.info("Sent chat response to {}: {}", destination, answer);
    }

    @MessageMapping("/chat/to-admin")
    public void sendMessageToAdmin(@Payload ChatMessageDTO chatMessage) {
        log.info("User {} sent message to admin: {}", chatMessage.getUserId(), chatMessage.getContent());
        // Forward to admin topic
        messagingTemplate.convertAndSend("/topic/admin/messages", chatMessage);
    }

    @MessageMapping("/chat/to-user")
    public void sendMessageToUser(@Payload ChatMessageDTO chatMessage) {
        log.info("Admin sent message to user {}: {}", chatMessage.getUserId(), chatMessage.getContent());
        // Forward to specific user topic
        messagingTemplate.convertAndSend("/topic/chat/" + chatMessage.getUserId(), chatMessage);
    }
}
