package com.example.authservice.consumer;

import com.example.authservice.event.UserEvent;
import com.example.authservice.service.CredentialsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {

    private final CredentialsService credentialsService;

    @RabbitListener(queues = "auth.user.events.queue")
    public void handleUserEvent(UserEvent event) {
        log.info("AuthService received user event: {}", event);

        switch (event.getEventType()) {
            case "DELETE":
                handleUserDeleted(event);
                break;
            case "UPDATE_EMAIL":
                handleUserEmailUpdated(event);
                break;
            default:
                log.warn("Unknown event type: {}", event.getEventType());
        }
    }

    private void handleUserDeleted(UserEvent event) {
        try {
            credentialsService.deleteByUserId(event.getUserId());
            log.info("Deleted credentials for userId: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Error deleting credentials for userId: {}", event.getUserId(), e);
        }
    }

    private void handleUserEmailUpdated(UserEvent event) {
        try {
            credentialsService.updateEmail(event.getUserId(), event.getEmail());
            log.info("Updated email for userId: {} from {} to {}",
                    event.getUserId(), event.getOldEmail(), event.getEmail());
        } catch (Exception e) {
            log.error("Error updating email for userId: {}", event.getUserId(), e);
        }
    }
}
