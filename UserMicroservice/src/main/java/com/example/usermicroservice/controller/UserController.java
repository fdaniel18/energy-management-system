package com.example.usermicroservice.controller;

import com.example.usermicroservice.dto.UserDto;
import com.example.usermicroservice.mapper.UserMapper;
import com.example.usermicroservice.model.User;
import com.example.usermicroservice.security.RequireRole;
import com.example.usermicroservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Endpoint public pentru crearea unui utilizator (fără autentificare)
    @PostMapping("/public")
    public ResponseEntity<UserDto> createPublic(@Valid @RequestBody UserDto userDto) {
        User user = UserMapper.toEntity(userDto);
        User created = userService.create(user);
        return ResponseEntity.created(URI.create("/users/" + created.getId())).body(UserMapper.toDto(created));
    }

    @RequireRole("ADMIN")
    @GetMapping
    public List<UserDto> getAll() {
        return userService.findAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @RequireRole("ADMIN")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable Long id) {
        return userService.findById(id)
                .map(UserMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @RequireRole("ADMIN")
    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody UserDto userDto) {
        User user = UserMapper.toEntity(userDto);
        User created = userService.create(user);
        return ResponseEntity.created(URI.create("/users/" + created.getId())).body(UserMapper.toDto(created));
    }

    @RequireRole("ADMIN")
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> update(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {
        User user = UserMapper.toEntity(userDto);
        User updated = userService.update(id, user);
        return ResponseEntity.ok(UserMapper.toDto(updated));
    }

    @RequireRole("ADMIN")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}