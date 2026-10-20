package com.example.authservice.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public UserClient(RestTemplate restTemplate,
                      @Value("${userservice.base-url:http://localhost:8080}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public UserDto createPublicUser(String email, String firstName, String lastName) {
        UserDto req = new UserDto(null, email, firstName, lastName);
        ResponseEntity<UserDto> response = restTemplate.postForEntity(baseUrl + "/users/public", req, UserDto.class);
        return response.getBody();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDto {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
    }
}

