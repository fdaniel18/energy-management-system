package com.example.websocketmicroservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private String sender; // "USER" or "SYSTEM"
    private String content;
    private Long userId;
}

