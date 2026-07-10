package com.example.demo.service;

import com.example.demo.dto.EnrollmentRequestDto;
import com.example.demo.dto.EnrollmentResponseDto;
import com.example.demo.entity.Opportunity;
import com.example.demo.entity.SystemUser;
import com.example.demo.entity.VolunteerEnrollment;
import com.example.demo.exception.BusinessValidationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.OpportunityRepository;
import com.example.demo.repository.SystemUserRepository;
import com.example.demo.repository.VolunteerEnrollmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EnrollmentService {

    private final VolunteerEnrollmentRepository enrollmentRepository;
    private final SystemUserRepository systemUserRepository;
    private final OpportunityRepository opportunityRepository;

    public EnrollmentService(VolunteerEnrollmentRepository enrollmentRepository,
                              SystemUserRepository systemUserRepository,
                              OpportunityRepository opportunityRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.systemUserRepository = systemUserRepository;
        this.opportunityRepository = opportunityRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public EnrollmentResponseDto enrollVolunteer(Long volunteerId, EnrollmentRequestDto dto) {

        SystemUser volunteer = systemUserRepository.findById(volunteerId)
                .orElseThrow(() -> new ResourceNotFoundException("SystemUser", volunteerId));

        if (volunteer.getRole() != SystemUser.Role.VOLUNTEER) {
            throw new BusinessValidationException("Only users with VOLUNTEER role can enroll.");
        }

        if (volunteer.getStatus() != SystemUser.UserStatus.APPROVED) {
            throw new BusinessValidationException("Your account must be APPROVED by an administrator before you can enroll.");
        }

        Opportunity opportunity = opportunityRepository.findById(dto.getOpportunityId())
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity", dto.getOpportunityId()));

        if (opportunity.getStatus() != Opportunity.OpportunityStatus.OPEN) {
            throw new BusinessValidationException("Cannot enroll in opportunity. Only OPEN opportunities accept enrollments.");
        }

        if (enrollmentRepository.existsByVolunteerIdAndOpportunityId(volunteerId, opportunity.getId())) {
            throw new BusinessValidationException("You are already enrolled in this opportunity.");
        }

        if (opportunity.getEnrolledCount() >= opportunity.getMaxCapacity()) {
            throw new BusinessValidationException("Opportunity is at full capacity.");
        }

        VolunteerEnrollment enrollment = VolunteerEnrollment.builder()
                .volunteer(volunteer)
                .opportunity(opportunity)
                .status(VolunteerEnrollment.EnrollmentStatus.PENDING)
                .notes(dto.getNotes())
                .build();

        VolunteerEnrollment saved = enrollmentRepository.save(enrollment);

        opportunity.setEnrolledCount(opportunity.getEnrolledCount() + 1);
        opportunityRepository.save(opportunity);

        return mapToResponseDto(saved);
    }

    @Transactional(rollbackFor = Exception.class)
    public EnrollmentResponseDto approveEnrollment(Long enrollmentId, SystemUser actor) {

        VolunteerEnrollment enrollment = findAndVerifyActor(enrollmentId, actor);

        if (enrollment.getStatus() != VolunteerEnrollment.EnrollmentStatus.PENDING) {
            throw new BusinessValidationException("Only PENDING enrollments can be approved.");
        }

        enrollment.setStatus(VolunteerEnrollment.EnrollmentStatus.APPROVED);
        enrollment.setApprovedAt(LocalDateTime.now());

        VolunteerEnrollment saved = enrollmentRepository.save(enrollment);
        return mapToResponseDto(saved);
    }

    @Transactional(rollbackFor = Exception.class)
    public EnrollmentResponseDto checkIn(Long enrollmentId, SystemUser actor) {

        VolunteerEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("VolunteerEnrollment", enrollmentId));

        if (actor.getRole() == SystemUser.Role.ORGANIZATION_COORDINATOR) {
            Long actorOrgId = actor.getOrganization() != null ? actor.getOrganization().getId() : null;
            Long enrollmentOrgId = enrollment.getOpportunity() != null && enrollment.getOpportunity().getOrganization() != null
                    ? enrollment.getOpportunity().getOrganization().getId() : null;
            if (actorOrgId == null || !actorOrgId.equals(enrollmentOrgId)) {
                throw new BusinessValidationException("Access Denied: You can only check in volunteers for your assigned organization.");
            }
        }

        if (enrollment.getStatus() != VolunteerEnrollment.EnrollmentStatus.APPROVED) {
            throw new BusinessValidationException("Volunteer must be APPROVED before checking in.");
        }

        enrollment.setStatus(VolunteerEnrollment.EnrollmentStatus.STARTED);
        enrollmentRepository.save(enrollment);

        VolunteerEnrollment withDetails = enrollmentRepository.findWithDetailsById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("VolunteerEnrollment", enrollmentId));
        return mapToResponseDto(withDetails);
    }

    @Transactional(rollbackFor = Exception.class)
    public EnrollmentResponseDto completeEnrollment(Long enrollmentId, Double hours, SystemUser actor) {

        VolunteerEnrollment enrollment = findAndVerifyActor(enrollmentId, actor);

        if (enrollment.getStatus() != VolunteerEnrollment.EnrollmentStatus.STARTED) {
            throw new BusinessValidationException("Volunteer must be in STARTED status before completion.");
        }

        enrollment.setStatus(VolunteerEnrollment.EnrollmentStatus.COMPLETED);
        enrollment.setHoursLogged(hours);

        VolunteerEnrollment saved = enrollmentRepository.save(enrollment);
        return mapToResponseDto(saved);
    }

    @Transactional(rollbackFor = Exception.class)
    public EnrollmentResponseDto rejectEnrollment(Long enrollmentId, SystemUser actor) {

        VolunteerEnrollment enrollment = findAndVerifyActor(enrollmentId, actor);

        if (enrollment.getStatus() != VolunteerEnrollment.EnrollmentStatus.PENDING) {
            throw new BusinessValidationException("Only PENDING enrollments can be rejected.");
        }

        Opportunity opportunity = enrollment.getOpportunity();
        if (opportunity != null) {
            int newCount = Math.max(0, opportunity.getEnrolledCount() - 1);
            opportunity.setEnrolledCount(newCount);
            opportunityRepository.save(opportunity);
        }

        enrollment.setStatus(VolunteerEnrollment.EnrollmentStatus.REJECTED);
        VolunteerEnrollment saved = enrollmentRepository.save(enrollment);
        return mapToResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<EnrollmentResponseDto> getAllEnrollments(Pageable pageable) {
        return enrollmentRepository.findAll(pageable).map(this::mapToResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<EnrollmentResponseDto> getEnrollmentsByVolunteer(Long volunteerId, Pageable pageable) {
        return enrollmentRepository.findByVolunteerId(volunteerId, pageable).map(this::mapToResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<EnrollmentResponseDto> getEnrollmentsByOrganization(Long orgId, Pageable pageable) {
        return enrollmentRepository.findByOrganizationId(orgId, pageable).map(this::mapToResponseDto);
    }

    /**
     * Loads the enrollment and, for ORGANIZATION_COORDINATOR actors, verifies they
     * may only manage enrollments belonging to opportunities of their own organization.
     */
    private VolunteerEnrollment findAndVerifyActor(Long enrollmentId, SystemUser actor) {
        VolunteerEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("VolunteerEnrollment", enrollmentId));

        if (actor.getRole() == SystemUser.Role.ORGANIZATION_COORDINATOR) {
            Long actorOrgId = actor.getOrganization() != null ? actor.getOrganization().getId() : null;
            Long enrollmentOrgId = enrollment.getOpportunity() != null && enrollment.getOpportunity().getOrganization() != null
                    ? enrollment.getOpportunity().getOrganization().getId() : null;
            if (actorOrgId == null || !actorOrgId.equals(enrollmentOrgId)) {
                throw new BusinessValidationException("Access Denied: You can only manage enrollments for your assigned organization.");
            }
        }

        return enrollment;
    }

    private EnrollmentResponseDto mapToResponseDto(VolunteerEnrollment enrollment) {
        return EnrollmentResponseDto.builder()
                .id(enrollment.getId())
                .volunteerId(enrollment.getVolunteer() != null ? enrollment.getVolunteer().getId() : null)
                .volunteerName(enrollment.getVolunteer() != null ? enrollment.getVolunteer().getFullName() : null)
                .opportunityId(enrollment.getOpportunity() != null ? enrollment.getOpportunity().getId() : null)
                .opportunityTitle(enrollment.getOpportunity() != null ? enrollment.getOpportunity().getTitle() : null)
                .organizationName(enrollment.getOpportunity() != null && enrollment.getOpportunity().getOrganization() != null
                        ? enrollment.getOpportunity().getOrganization().getName() : null)
                .status(enrollment.getStatus() != null ? enrollment.getStatus().name() : null)
                .enrolledAt(enrollment.getEnrolledAt())
                .approvedAt(enrollment.getApprovedAt())
                .hoursLogged(enrollment.getHoursLogged())
                .notes(enrollment.getNotes())
                .build();
    }
}
