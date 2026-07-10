package com.example.demo.repository;

import com.example.demo.entity.Opportunity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    List<Opportunity> findByOrganizationId(Long organizationId);

    List<Opportunity> findByStatus(Opportunity.OpportunityStatus status);

    Page<Opportunity> findAll(Pageable pageable);

    Page<Opportunity> findByOrganizationId(Long organizationId, Pageable pageable);

    long countByStatus(Opportunity.OpportunityStatus status);

    @Query("SELECT o FROM Opportunity o WHERE o.status = 'OPEN' AND o.enrolledCount < o.maxCapacity")
    List<Opportunity> findAvailableOpportunities();

    @Query("SELECT o FROM Opportunity o WHERE o.organization.id = :orgId AND o.status = :status")
    List<Opportunity> findByOrganizationIdAndStatus(@Param("orgId") Long orgId,
                                                      @Param("status") Opportunity.OpportunityStatus status);

    @Query("SELECT COUNT(o) FROM Opportunity o WHERE o.organization.id = :orgId")
    long countByOrganizationId(@Param("orgId") Long orgId);
}
