package com.example.demo.controller;

import com.example.demo.dto.ImpactReportRequestDto;
import com.example.demo.dto.ImpactReportResponseDto;
import com.example.demo.dto.ImpactSummaryDto;
import com.example.demo.entity.SystemUser;
import com.example.demo.service.ImpactReportService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/impact-reports")
public class ImpactReportController {

    private final ImpactReportService service;

    public ImpactReportController(ImpactReportService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<ImpactReportResponseDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ImpactReportResponseDto> result =
                service.getAllReports(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt")));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/my")
    public ResponseEntity<Page<ImpactReportResponseDto>> getMyReports(
            @AuthenticationPrincipal SystemUser principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ImpactReportResponseDto> result = service.getReportsByVolunteer(principal.getId(), PageRequest.of(page, size));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/organization/{orgId}")
    public ResponseEntity<List<ImpactReportResponseDto>> getByOrganization(@PathVariable Long orgId) {
        return ResponseEntity.ok(service.getReportsByOrganization(orgId));
    }

    @PostMapping
    public ResponseEntity<ImpactReportResponseDto> create(@AuthenticationPrincipal SystemUser principal,
                                                            @Valid @RequestBody ImpactReportRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.submitReport(principal.getId(), dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImpactReportResponseDto> update(@AuthenticationPrincipal SystemUser principal,
                                                            @PathVariable Long id,
                                                            @Valid @RequestBody ImpactReportRequestDto dto) {
        return ResponseEntity.ok(service.updateReport(id, principal.getId(), dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal SystemUser principal, @PathVariable Long id) {
        boolean isAdmin = principal.getRole() == SystemUser.Role.PLATFORM_ADMIN;
        service.deleteReport(id, principal.getId(), isAdmin);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImpactReportResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getReportById(id));
    }

    @GetMapping("/volunteer/{volunteerId}/summary")
    public ResponseEntity<ImpactSummaryDto> getVolunteerSummary(@PathVariable Long volunteerId) {
        return ResponseEntity.ok(service.getVolunteerImpactSummary(volunteerId));
    }

    @GetMapping("/organization/{orgId}/summary")
    public ResponseEntity<ImpactSummaryDto> getOrganizationSummary(@PathVariable Long orgId) {
        return ResponseEntity.ok(service.getOrganizationImpactSummary(orgId));
    }
}
