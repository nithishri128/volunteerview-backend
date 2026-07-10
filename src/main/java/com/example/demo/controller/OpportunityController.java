package com.example.demo.controller;

import com.example.demo.dto.OpportunityRequestDto;
import com.example.demo.dto.OpportunityResponseDto;
import com.example.demo.entity.SystemUser;
import com.example.demo.exception.BusinessValidationException;
import com.example.demo.repository.SystemUserRepository;
import com.example.demo.service.OpportunityService;
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
@RequestMapping("/api/opportunities")
public class OpportunityController {

    private final OpportunityService service;
    private final SystemUserRepository userRepository;

    public OpportunityController(OpportunityService service, SystemUserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<Page<OpportunityResponseDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<OpportunityResponseDto> result =
                service.getAllOpportunities(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OpportunityResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getOpportunityById(id));
    }

    @GetMapping("/organization/{orgId}")
    public ResponseEntity<List<OpportunityResponseDto>> getByOrganization(@PathVariable Long orgId) {
        return ResponseEntity.ok(service.getOpportunitiesByOrganization(orgId));
    }

    @PostMapping
    public ResponseEntity<OpportunityResponseDto> create(@AuthenticationPrincipal Object principal,
                                                          @Valid @RequestBody OpportunityRequestDto dto) {
        SystemUser actor = getAttachedUser(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createOpportunity(dto, actor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OpportunityResponseDto> update(@AuthenticationPrincipal Object principal,
                                                          @PathVariable Long id,
                                                          @Valid @RequestBody OpportunityRequestDto dto) {
        SystemUser actor = getAttachedUser(principal);
        return ResponseEntity.ok(service.updateOpportunity(id, dto, actor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Object principal, @PathVariable Long id) {
        SystemUser actor = getAttachedUser(principal);
        service.deleteOpportunity(id, actor);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<OpportunityResponseDto> publish(@AuthenticationPrincipal Object principal, @PathVariable Long id) {
        SystemUser actor = getAttachedUser(principal);
        return ResponseEntity.ok(service.publishOpportunity(id, actor));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<OpportunityResponseDto> close(@AuthenticationPrincipal Object principal, @PathVariable Long id) {
        SystemUser actor = getAttachedUser(principal);
        return ResponseEntity.ok(service.closeOpportunity(id, actor));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<OpportunityResponseDto> complete(@AuthenticationPrincipal Object principal, @PathVariable Long id) {
        SystemUser actor = getAttachedUser(principal);
        return ResponseEntity.ok(service.completeOpportunity(id, actor));
    }

    /**
     * Ensures we operate on an attached JPA entity rather than the detached
     * principal object stored in the security context.
     */
    private SystemUser getAttachedUser(Object principal) {
        if (principal == null) {
            throw new BusinessValidationException("Authentication required to create opportunities.");
        }
        SystemUser principalUser = (SystemUser) principal;
        return userRepository.findById(principalUser.getId())
                .orElseThrow(() -> new BusinessValidationException("Authentication required to create opportunities."));
    }
}
