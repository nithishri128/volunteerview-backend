package com.example.demo.controller;

import com.example.demo.dto.EnrollmentRequestDto;
import com.example.demo.dto.EnrollmentResponseDto;
import com.example.demo.entity.SystemUser;
import com.example.demo.exception.BusinessValidationException;
import com.example.demo.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService service;

    public EnrollmentController(EnrollmentService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<EnrollmentResponseDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<EnrollmentResponseDto> result =
                service.getAllEnrollments(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "enrolledAt")));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/my")
    public ResponseEntity<Page<EnrollmentResponseDto>> getMyEnrollments(
            @AuthenticationPrincipal SystemUser principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (principal == null) {
            throw new BusinessValidationException("You must be logged in as a volunteer to view your enrollments.");
        }
        Page<EnrollmentResponseDto> result = service.getEnrollmentsByVolunteer(
                principal.getId(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "enrolledAt")));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/organization/{orgId}")
    public ResponseEntity<Page<EnrollmentResponseDto>> getByOrganization(
            @PathVariable Long orgId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<EnrollmentResponseDto> result = service.getEnrollmentsByOrganization(orgId, PageRequest.of(page, size));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/enroll")
    public ResponseEntity<EnrollmentResponseDto> enroll(@AuthenticationPrincipal SystemUser principal,
                                                          @Valid @RequestBody EnrollmentRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.enrollVolunteer(principal.getId(), dto));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<EnrollmentResponseDto> approve(@AuthenticationPrincipal SystemUser principal,
                                                          @PathVariable Long id) {
        return ResponseEntity.ok(service.approveEnrollment(id, principal));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<EnrollmentResponseDto> reject(@AuthenticationPrincipal SystemUser principal,
                                                         @PathVariable Long id) {
        return ResponseEntity.ok(service.rejectEnrollment(id, principal));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<EnrollmentResponseDto> complete(@AuthenticationPrincipal SystemUser principal,
                                                           @PathVariable Long id,
                                                           @RequestParam Double hours) {
        return ResponseEntity.ok(service.completeEnrollment(id, hours, principal));
    }

    @PatchMapping("/{id}/check-in")
    public ResponseEntity<EnrollmentResponseDto> checkIn(@AuthenticationPrincipal SystemUser principal,
                                                          @PathVariable Long id) {
        return ResponseEntity.ok(service.checkIn(id, principal));
    }
}
