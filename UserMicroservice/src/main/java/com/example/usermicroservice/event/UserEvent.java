package com.example.usermicroservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent implements Serializable {
    private String eventType; // DELETE, UPDATE_EMAIL
    private Long userId;
    private String email;
    private String oldEmail;
}
