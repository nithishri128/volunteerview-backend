package com.example.demo.controller;

import com.example.demo.dto.UserResponseDto;
import com.example.demo.dto.UserUpdateDto;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(service.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id,
                                                        @Valid @RequestBody UserUpdateDto dto) {
        return ResponseEntity.ok(service.updateUser(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        service.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UserResponseDto> updateStatus(@PathVariable Long id,
                                                          @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(service.updateStatus(id, payload.get("status")));
    }

    @PatchMapping("/{id}/assign-organization/{orgId}")
    public ResponseEntity<UserResponseDto> assignOrganization(@PathVariable Long id, @PathVariable Long orgId) {
        return ResponseEntity.ok(service.assignOrganization(id, orgId));
    }
}
