package com.example.usermicroservice.publisher;

import com.example.usermicroservice.config.RabbitMQConfig;
import com.example.usermicroservice.event.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishUserDeleted(Long userId, String email) {
        UserEvent event = new UserEvent("DELETE", userId, email, null);
        rabbitTemplate.convertAndSend(RabbitMQConfig.USER_EVENTS_EXCHANGE, "", event);
        log.info("Published user deleted event: userId={}, email={}", userId, email);
    }

    public void publishUserEmailUpdated(Long userId, String newEmail, String oldEmail) {
        UserEvent event = new UserEvent("UPDATE_EMAIL", userId, newEmail, oldEmail);
        rabbitTemplate.convertAndSend(RabbitMQConfig.USER_EVENTS_EXCHANGE, "", event);
        log.info("Published user email updated event: userId={}, newEmail={}, oldEmail={}", userId, newEmail, oldEmail);
    }
}
