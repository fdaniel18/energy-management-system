package com.example.authservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CredentialsDto {

    private Long id;       // ID-ul intern al credentialelor
    private Long userId;   // ID-ul utilizatorului din UserService
    private String email;  // email-ul de autentificare
    private String role;   // rolul (USER / ADMIN)
}
