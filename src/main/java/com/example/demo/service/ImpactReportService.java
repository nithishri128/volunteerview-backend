package com.example.demo.service;

import com.example.demo.dto.ImpactReportRequestDto;
import com.example.demo.dto.ImpactReportResponseDto;
import com.example.demo.dto.ImpactSummaryDto;
import com.example.demo.entity.ImpactReport;
import com.example.demo.entity.VolunteerEnrollment;
import com.example.demo.exception.BusinessValidationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ImpactReportRepository;
import com.example.demo.repository.VolunteerEnrollmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImpactReportService {

    private final ImpactReportRepository impactReportRepository;
    private final VolunteerEnrollmentRepository enrollmentRepository;

    public ImpactReportService(ImpactReportRepository impactReportRepository,
                                VolunteerEnrollmentRepository enrollmentRepository) {
        this.impactReportRepository = impactReportRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional
    public ImpactReportResponseDto submitReport(Long volunteerId, ImpactReportRequestDto dto) {

        VolunteerEnrollment enrollment = enrollmentRepository.findById(dto.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("VolunteerEnrollment", dto.getEnrollmentId()));

        if (enrollment.getVolunteer() == null || !enrollment.getVolunteer().getId().equals(volunteerId)) {
            throw new BusinessValidationException("You can only submit impact reports for your own enrollments");
        }

        if (enrollment.getStatus() != VolunteerEnrollment.EnrollmentStatus.COMPLETED) {
            throw new BusinessValidationException(
                    "Impact reports can only be submitted for COMPLETED enrollments. Current status: " + enrollment.getStatus());
        }

        if (impactReportRepository.existsByEnrollmentId(enrollment.getId())) {
            throw new BusinessValidationException("An impact report has already been submitted for this enrollment");
        }

        double loggedHours = enrollment.getHoursLogged() != null ? enrollment.getHoursLogged() : 0.0;
        if (dto.getHoursContributed() > loggedHours * 1.5) {
            throw new BusinessValidationException(
                    "Reported hours (" + dto.getHoursContributed() + ") significantly exceed logged hours ("
                            + enrollment.getHoursLogged() + "). Please verify your hours.");
        }

        ImpactReport report = ImpactReport.builder()
                .enrollment(enrollment)
                .volunteer(enrollment.getVolunteer())
                .summary(dto.getSummary())
                .hoursContributed(dto.getHoursContributed())
                .beneficiariesServed(dto.getBeneficiariesServed())
                .rating(dto.getRating())
                .build();

        ImpactReport saved = impactReportRepository.save(report);
        return mapToResponseDto(saved);
    }

    @Transactional
    public ImpactReportResponseDto updateReport(Long reportId, Long volunteerId, ImpactReportRequestDto dto) {

        ImpactReport report = impactReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("ImpactReport", reportId));

        if (report.getVolunteer() == null || !report.getVolunteer().getId().equals(volunteerId)) {
            throw new BusinessValidationException("You can only update your own impact reports");
        }

        double loggedHours = report.getEnrollment() != null && report.getEnrollment().getHoursLogged() != null
                ? report.getEnrollment().getHoursLogged() : 0.0;
        if (dto.getHoursContributed() > loggedHours * 1.5) {
            throw new BusinessValidationException(
                    "Reported hours (" + dto.getHoursContributed() + ") significantly exceed logged hours ("
                            + loggedHours + "). Please verify your hours.");
        }

        report.setSummary(dto.getSummary());
        report.setHoursContributed(dto.getHoursContributed());
        report.setBeneficiariesServed(dto.getBeneficiariesServed());
        report.setRating(dto.getRating());

        ImpactReport saved = impactReportRepository.save(report);
        return mapToResponseDto(saved);
    }

    @Transactional
    public void deleteReport(Long reportId, Long volunteerId, boolean isAdmin) {
        ImpactReport report = impactReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("ImpactReport", reportId));

        if (!isAdmin && (report.getVolunteer() == null || !report.getVolunteer().getId().equals(volunteerId))) {
            throw new BusinessValidationException("You can only delete your own impact reports");
        }

        impactReportRepository.delete(report);
    }

    @Transactional(readOnly = true)
    public ImpactSummaryDto getVolunteerImpactSummary(Long volunteerId) {
        Double totalHours = impactReportRepository.sumHoursContributedByVolunteerId(volunteerId);
        Integer totalBeneficiaries = impactReportRepository.sumBeneficiariesServedByVolunteerId(volunteerId);
        Double avgRating = impactReportRepository.avgRatingByVolunteerId(volunteerId);
        long totalReports = impactReportRepository.findByVolunteerId(volunteerId).size();

        return ImpactSummaryDto.builder()
                .totalHoursContributed(totalHours)
                .totalBeneficiariesServed(totalBeneficiaries)
                .averageRating(avgRating)
                .totalReports(totalReports)
                .build();
    }

    @Transactional(readOnly = true)
    public ImpactSummaryDto getOrganizationImpactSummary(Long orgId) {
        Double totalHours = impactReportRepository.sumHoursContributedByOrganizationId(orgId);
        Integer totalBeneficiaries = impactReportRepository.sumBeneficiariesServedByOrganizationId(orgId);
        long totalReports = impactReportRepository.countByOrganizationId(orgId);

        return ImpactSummaryDto.builder()
                .totalHoursContributed(totalHours)
                .totalBeneficiariesServed(totalBeneficiaries)
                .averageRating(0.0)
                .totalReports(totalReports)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<ImpactReportResponseDto> getAllReports(Pageable pageable) {
        return impactReportRepository.findAll(pageable).map(this::mapToResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<ImpactReportResponseDto> getReportsByVolunteer(Long volunteerId, Pageable pageable) {
        return impactReportRepository.findByVolunteerId(volunteerId, pageable).map(this::mapToResponseDto);
    }

    @Transactional(readOnly = true)
    public List<ImpactReportResponseDto> getReportsByOrganization(Long orgId) {
        return impactReportRepository.findByOrganizationId(orgId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ImpactReportResponseDto getReportById(Long id) {
        ImpactReport report = impactReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ImpactReport", id));
        return mapToResponseDto(report);
    }

    private ImpactReportResponseDto mapToResponseDto(ImpactReport report) {
        VolunteerEnrollment enrollment = report.getEnrollment();
        return ImpactReportResponseDto.builder()
                .id(report.getId())
                .enrollmentId(enrollment != null ? enrollment.getId() : null)
                .volunteerId(report.getVolunteer() != null ? report.getVolunteer().getId() : null)
                .volunteerName(report.getVolunteer() != null ? report.getVolunteer().getFullName() : null)
                .opportunityTitle(enrollment != null && enrollment.getOpportunity() != null
                        ? enrollment.getOpportunity().getTitle() : null)
                .organizationName(enrollment != null && enrollment.getOpportunity() != null
                        && enrollment.getOpportunity().getOrganization() != null
                        ? enrollment.getOpportunity().getOrganization().getName() : null)
                .summary(report.getSummary())
                .hoursContributed(report.getHoursContributed())
                .beneficiariesServed(report.getBeneficiariesServed())
                .rating(report.getRating())
                .submittedAt(report.getSubmittedAt())
                .build();
    }
}
