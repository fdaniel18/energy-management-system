package com.example.websocketmicroservice.consumer;

import com.example.websocketmicroservice.config.RabbitMQConfig;
import com.example.websocketmicroservice.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void receiveNotification(NotificationDTO notification) {
        log.info("Received notification: {}", notification);

        if (notification.getUserId() != null) {
            // Send to specific user topic
            String destination = "/topic/notifications/" + notification.getUserId();
            messagingTemplate.convertAndSend(destination, notification);
            log.info("Sent notification to WebSocket destination: {}", destination);
        } else {
            log.warn("Notification received without userId, cannot route to WebSocket: {}", notification);
        }
    }
}

