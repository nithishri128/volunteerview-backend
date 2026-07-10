package com.example.demo.controller;

import com.example.demo.dto.OrganizationRequestDto;
import com.example.demo.dto.OrganizationResponseDto;
import com.example.demo.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService service;

    public OrganizationController(OrganizationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<OrganizationResponseDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<OrganizationResponseDto> result =
                service.getAllOrganizations(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getOrganizationById(id));
    }

    @GetMapping("/coordinator/{coordinatorId}")
    public ResponseEntity<List<OrganizationResponseDto>> getByCoordinator(@PathVariable Long coordinatorId) {
        return ResponseEntity.ok(service.getOrganizationsByCoordinator(coordinatorId));
    }

    @PostMapping
    public ResponseEntity<OrganizationResponseDto> create(@Valid @RequestBody OrganizationRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createOrganization(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganizationResponseDto> update(@PathVariable Long id,
                                                           @Valid @RequestBody OrganizationRequestDto dto) {
        return ResponseEntity.ok(service.updateOrganization(id, dto));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<OrganizationResponseDto> approve(@PathVariable Long id) {
        return ResponseEntity.ok(service.approveOrganization(id));
    }

    @PatchMapping("/{id}/suspend")
    public ResponseEntity<OrganizationResponseDto> suspend(@PathVariable Long id) {
        return ResponseEntity.ok(service.suspendOrganization(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteOrganization(id);
        return ResponseEntity.noContent().build();
    }
}
