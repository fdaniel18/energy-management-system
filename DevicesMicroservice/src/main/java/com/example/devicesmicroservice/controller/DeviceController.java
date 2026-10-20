package com.example.devicesmicroservice.controller;

import com.example.devicesmicroservice.dto.DeviceDto;
import com.example.devicesmicroservice.mapper.DeviceMapper;
import com.example.devicesmicroservice.model.Device;
import com.example.devicesmicroservice.security.RequireRole;
import com.example.devicesmicroservice.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @RequireRole("ADMIN")
    @GetMapping
    public List<DeviceDto> getAll() {
        return deviceService.findAll().stream()
                .map(DeviceMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id, HttpServletRequest request) {
        Optional<Device> opt = deviceService.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Device device = opt.get();

        String role = getHeader(request, "X-User-Role");
        if ("ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.ok(DeviceMapper.toDto(device));
        }

        Long callerUserId = getHeaderAsLong(request, "X-User-Id");
        if (callerUserId != null && callerUserId.equals(device.getUserId())) {
            return ResponseEntity.ok(DeviceMapper.toDto(device));
        }
        return ResponseEntity.status(403).body("Forbidden: not owner");
    }

    @RequireRole("ADMIN")
    @PostMapping
    public ResponseEntity<DeviceDto> create(@Valid @RequestBody DeviceDto deviceDto) {
        Device device = DeviceMapper.toEntity(deviceDto);
        Device created = deviceService.create(device);
        return ResponseEntity.created(URI.create("/devices/" + created.getId())).body(DeviceMapper.toDto(created));
    }

    @RequireRole("ADMIN")
    @PutMapping("/{id}")
    public ResponseEntity<DeviceDto> update(@PathVariable Long id, @Valid @RequestBody DeviceDto deviceDto) {
        Device device = DeviceMapper.toEntity(deviceDto);
        Device updated = deviceService.update(id, device);
        return ResponseEntity.ok(DeviceMapper.toDto(updated));
    }

    @RequireRole("ADMIN")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deviceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getByUserId(@PathVariable Long userId, HttpServletRequest request) {
        String role = getHeader(request, "X-User-Role");
        if (!"ADMIN".equalsIgnoreCase(role)) {
            Long callerUserId = getHeaderAsLong(request, "X-User-Id");
            if (callerUserId == null || !callerUserId.equals(userId)) {
                return ResponseEntity.status(403).body("Forbidden: can only access own devices");
            }
        }
        List<DeviceDto> list = deviceService.findByUserId(userId).stream()
                .map(DeviceMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    // Internal endpoint for inter-service communication (no auth required)
    @GetMapping("/internal/all")
    public List<DeviceDto> getAllInternal() {
        return deviceService.findAll().stream()
                .map(DeviceMapper::toDto)
                .collect(Collectors.toList());
    }

    private static String getHeader(HttpServletRequest request, String name) {
        String v = request.getHeader(name);
        return (v == null || v.isBlank()) ? null : v;
        }

    private static Long getHeaderAsLong(HttpServletRequest request, String name) {
        String v = getHeader(request, name);
        if (v == null) return null;
        try { return Long.valueOf(v); } catch (NumberFormatException e) { return null; }
    }
}
