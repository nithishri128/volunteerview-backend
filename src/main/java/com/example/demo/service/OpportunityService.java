package com.example.demo.service;

import com.example.demo.dto.OpportunityRequestDto;
import com.example.demo.dto.OpportunityResponseDto;
import com.example.demo.entity.Opportunity;
import com.example.demo.entity.Organization;
import com.example.demo.entity.SystemUser;
import com.example.demo.exception.BusinessValidationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.OpportunityRepository;
import com.example.demo.repository.OrganizationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final OrganizationRepository organizationRepository;

    public OpportunityService(OpportunityRepository opportunityRepository, OrganizationRepository organizationRepository) {
        this.opportunityRepository = opportunityRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public OpportunityResponseDto createOpportunity(OpportunityRequestDto dto, SystemUser actor) {

        if (actor.getRole() == SystemUser.Role.ORGANIZATION_COORDINATOR) {
            if (actor.getOrganization() == null) {
                throw new BusinessValidationException("You must be assigned to an organization before creating opportunities.");
            }
            if (!actor.getOrganization().getId().equals(dto.getOrganizationId())) {
                throw new BusinessValidationException("Access Denied: You can only create opportunities for your assigned organization.");
            }
        }

        Organization organization = organizationRepository.findById(dto.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization", dto.getOrganizationId()));

        Opportunity opportunity = Opportunity.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .location(dto.getLocation())
                .category(dto.getCategory())
                .scheduledDate(LocalDate.parse(dto.getScheduledDate()))
                .startTime(LocalTime.parse(dto.getStartTime()))
                .endTime(LocalTime.parse(dto.getEndTime()))
                .maxCapacity(dto.getMaxCapacity())
                .requiredSkills(dto.getRequiredSkills())
                .organization(organization)
                .status(Opportunity.OpportunityStatus.DRAFT)
                .enrolledCount(0)
                .build();

        Opportunity saved = opportunityRepository.save(opportunity);
        return mapToResponseDto(saved);
    }

    @Transactional(rollbackFor = Exception.class)
    public OpportunityResponseDto updateOpportunity(Long id, OpportunityRequestDto dto, SystemUser actor) {

        Opportunity opportunity = findAndVerifyActor(id, actor);

        opportunity.setTitle(dto.getTitle());
        opportunity.setDescription(dto.getDescription());
        opportunity.setCategory(dto.getCategory());
        opportunity.setScheduledDate(LocalDate.parse(dto.getScheduledDate()));
        opportunity.setStartTime(LocalTime.parse(dto.getStartTime()));
        opportunity.setEndTime(LocalTime.parse(dto.getEndTime()));
        opportunity.setLocation(dto.getLocation());
        opportunity.setMaxCapacity(dto.getMaxCapacity());
        opportunity.setRequiredSkills(dto.getRequiredSkills());

        Opportunity saved = opportunityRepository.save(opportunity);
        return mapToResponseDto(saved);
    }

    @Transactional(rollbackFor = Exception.class)
    public OpportunityResponseDto publishOpportunity(Long id, SystemUser actor) {
        Opportunity opportunity = findAndVerifyActor(id, actor);
        opportunity.setStatus(Opportunity.OpportunityStatus.OPEN);
        Opportunity saved = opportunityRepository.save(opportunity);
        return mapToResponseDto(saved);
    }

    @Transactional(rollbackFor = Exception.class)
    public OpportunityResponseDto closeOpportunity(Long id, SystemUser actor) {
        Opportunity opportunity = findAndVerifyActor(id, actor);
        opportunity.setStatus(Opportunity.OpportunityStatus.CLOSED);
        Opportunity saved = opportunityRepository.save(opportunity);
        return mapToResponseDto(saved);
    }

    @Transactional(rollbackFor = Exception.class)
    public OpportunityResponseDto completeOpportunity(Long id, SystemUser actor) {
        Opportunity opportunity = findAndVerifyActor(id, actor);
        opportunity.setStatus(Opportunity.OpportunityStatus.COMPLETED);
        Opportunity saved = opportunityRepository.save(opportunity);
        return mapToResponseDto(saved);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteOpportunity(Long id, SystemUser actor) {
        Opportunity opportunity = findAndVerifyActor(id, actor);
        opportunityRepository.delete(opportunity);
    }

    @Transactional(readOnly = true)
    public Page<OpportunityResponseDto> getAllOpportunities(Pageable pageable) {
        return opportunityRepository.findAll(pageable).map(this::mapToResponseDto);
    }

    @Transactional(readOnly = true)
    public OpportunityResponseDto getOpportunityById(Long id) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity", id));
        return mapToResponseDto(opportunity);
    }

    @Transactional(readOnly = true)
    public List<OpportunityResponseDto> getOpportunitiesByOrganization(Long orgId) {
        return opportunityRepository.findByOrganizationId(orgId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Loads the opportunity and, for ORGANIZATION_COORDINATOR actors, verifies they
     * may only manage opportunities belonging to their own assigned organization.
     */
    private Opportunity findAndVerifyActor(Long id, SystemUser actor) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity", id));

        if (actor.getRole() == SystemUser.Role.ORGANIZATION_COORDINATOR) {
            Long actorOrgId = actor.getOrganization() != null ? actor.getOrganization().getId() : null;
            Long oppOrgId = opportunity.getOrganization() != null ? opportunity.getOrganization().getId() : null;
            if (actorOrgId == null || !actorOrgId.equals(oppOrgId)) {
                throw new BusinessValidationException("Access Denied: You can only manage opportunities for your assigned organization.");
            }
        }

        return opportunity;
    }

    private OpportunityResponseDto mapToResponseDto(Opportunity opportunity) {
        return OpportunityResponseDto.builder()
                .id(opportunity.getId())
                .title(opportunity.getTitle())
                .description(opportunity.getDescription())
                .location(opportunity.getLocation())
                .category(opportunity.getCategory())
                .scheduledDate(opportunity.getScheduledDate())
                .startTime(opportunity.getStartTime())
                .endTime(opportunity.getEndTime())
                .maxCapacity(opportunity.getMaxCapacity())
                .enrolledCount(opportunity.getEnrolledCount())
                .requiredSkills(opportunity.getRequiredSkills())
                .status(opportunity.getStatus().name())
                .organizationId(opportunity.getOrganization() != null ? opportunity.getOrganization().getId() : null)
                .organizationName(opportunity.getOrganization() != null ? opportunity.getOrganization().getName() : null)
                .isEnrolled(false)
                .createdAt(opportunity.getCreatedAt())
                .updatedAt(opportunity.getUpdatedAt())
                .build();
    }
}
