package com.example.usermicroservice.service;

import com.example.usermicroservice.model.User;
import com.example.usermicroservice.repository.UserRepository;
import com.example.usermicroservice.publisher.UserEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserEventPublisher userEventPublisher;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User create(User user) {
        user.setId(null); // ensure id is null for creation
        return userRepository.save(user);
    }

    public User update(Long id, User user) {
        return userRepository.findById(id)
                .map(existing -> {
                    String oldEmail = existing.getEmail();
                    String newEmail = user.getEmail();

                    existing.setEmail(newEmail);
                    existing.setFirstName(user.getFirstName());
                    existing.setLastName(user.getLastName());
                    User updated = userRepository.save(existing);

                    // Publish email update event if email changed
                    if (!oldEmail.equals(newEmail)) {
                        userEventPublisher.publishUserEmailUpdated(id, newEmail, oldEmail);
                    }

                    return updated;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Publish delete event before deleting
        userEventPublisher.publishUserDeleted(id, user.getEmail());

        userRepository.deleteById(id);
    }
}
