package com.example.authservice.mapper;

import com.example.authservice.dto.CredentialsDto;
import com.example.authservice.model.Credentials;
import org.springframework.stereotype.Component;

@Component
public class CredentialsMapper {

    public CredentialsDto toDto(Credentials credentials) {
        if (credentials == null) return null;

        return CredentialsDto.builder()
                .id(credentials.getId())
                .userId(credentials.getUserId())
                .email(credentials.getEmail())
                .role(credentials.getRole())
                .build();
    }

    public Credentials toEntity(CredentialsDto dto) {
        if (dto == null) return null;

        return Credentials.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .email(dto.getEmail())
                .role(dto.getRole())
                .build();
    }
}
