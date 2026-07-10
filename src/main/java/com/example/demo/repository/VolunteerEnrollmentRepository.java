package com.example.demo.repository;

import com.example.demo.entity.VolunteerEnrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VolunteerEnrollmentRepository extends JpaRepository<VolunteerEnrollment, Long> {

    boolean existsByVolunteerIdAndOpportunityId(Long volunteerId, Long opportunityId);

    @Query("SELECT ve FROM VolunteerEnrollment ve " +
            "JOIN FETCH ve.volunteer " +
            "JOIN FETCH ve.opportunity " +
            "JOIN FETCH ve.opportunity.organization " +
            "WHERE ve.id = :id")
    Optional<VolunteerEnrollment> findWithDetailsById(@Param("id") Long id);

    List<VolunteerEnrollment> findByVolunteerId(Long volunteerId);

    List<VolunteerEnrollment> findByOpportunityId(Long opportunityId);

    @EntityGraph(attributePaths = {"volunteer", "opportunity", "opportunity.organization"})
    Page<VolunteerEnrollment> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"volunteer", "opportunity", "opportunity.organization"})
    Page<VolunteerEnrollment> findByVolunteerId(Long volunteerId, Pageable pageable);

    List<VolunteerEnrollment> findByStatus(VolunteerEnrollment.EnrollmentStatus status);

    long countByStatus(VolunteerEnrollment.EnrollmentStatus status);

    @Query("SELECT ve FROM VolunteerEnrollment ve WHERE ve.opportunity.id = :oppId AND ve.status = :status")
    List<VolunteerEnrollment> findByOpportunityIdAndStatus(@Param("oppId") Long oppId,
                                                             @Param("status") VolunteerEnrollment.EnrollmentStatus status);

    @Query("SELECT SUM(ve.hoursLogged) FROM VolunteerEnrollment ve WHERE ve.volunteer.id = :volunteerId")
    Optional<Double> sumHoursLoggedByVolunteerId(@Param("volunteerId") Long volunteerId);

    @Query("SELECT COUNT(ve) FROM VolunteerEnrollment ve WHERE ve.opportunity.organization.id = :orgId")
    long countByOrganizationId(@Param("orgId") Long orgId);

    @EntityGraph(attributePaths = {"volunteer", "opportunity", "opportunity.organization"})
    @Query("SELECT ve FROM VolunteerEnrollment ve WHERE ve.opportunity.organization.id = :orgId")
    Page<VolunteerEnrollment> findByOrganizationId(@Param("orgId") Long orgId, Pageable pageable);
}
