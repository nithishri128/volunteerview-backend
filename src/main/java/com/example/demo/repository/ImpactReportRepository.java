package com.example.demo.repository;

import com.example.demo.entity.ImpactReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ImpactReportRepository extends JpaRepository<ImpactReport, Long> {

    boolean existsByEnrollmentId(Long enrollmentId);

    List<ImpactReport> findByVolunteerId(Long volunteerId);

    @EntityGraph(attributePaths = {"enrollment", "volunteer", "enrollment.opportunity", "enrollment.opportunity.organization"})
    Page<ImpactReport> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"enrollment", "volunteer", "enrollment.opportunity", "enrollment.opportunity.organization"})
    Page<ImpactReport> findByVolunteerId(Long volunteerId, Pageable pageable);

    @Query("SELECT ir FROM ImpactReport ir WHERE ir.enrollment.opportunity.organization.id = :orgId")
    List<ImpactReport> findByOrganizationId(@Param("orgId") Long orgId);

    @Query("SELECT COALESCE(SUM(ir.hoursContributed), 0) FROM ImpactReport ir WHERE ir.volunteer.id = :volunteerId")
    Double sumHoursContributedByVolunteerId(@Param("volunteerId") Long volunteerId);

    @Query("SELECT COALESCE(SUM(ir.beneficiariesServed), 0) FROM ImpactReport ir WHERE ir.volunteer.id = :volunteerId")
    Integer sumBeneficiariesServedByVolunteerId(@Param("volunteerId") Long volunteerId);

    @Query("SELECT COALESCE(AVG(ir.rating), 0) FROM ImpactReport ir WHERE ir.volunteer.id = :volunteerId")
    Double avgRatingByVolunteerId(@Param("volunteerId") Long volunteerId);

    @Query("SELECT COALESCE(SUM(ir.hoursContributed), 0) FROM ImpactReport ir WHERE ir.enrollment.opportunity.organization.id = :orgId")
    Double sumHoursContributedByOrganizationId(@Param("orgId") Long orgId);

    @Query("SELECT COALESCE(SUM(ir.beneficiariesServed), 0) FROM ImpactReport ir WHERE ir.enrollment.opportunity.organization.id = :orgId")
    Integer sumBeneficiariesServedByOrganizationId(@Param("orgId") Long orgId);

    @Query("SELECT COUNT(ir) FROM ImpactReport ir WHERE ir.enrollment.opportunity.organization.id = :orgId")
    long countByOrganizationId(@Param("orgId") Long orgId);
}
