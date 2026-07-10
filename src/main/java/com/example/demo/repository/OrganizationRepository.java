package com.example.demo.repository;

import com.example.demo.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    boolean existsByName(String name);

    Optional<Organization> findByCoordinatorId(Long coordinatorId);

    List<Organization> findByStatus(Organization.OrganizationStatus status);

    Page<Organization> findAll(Pageable pageable);

    long countByStatus(Organization.OrganizationStatus status);

    @Query("SELECT o FROM Organization o WHERE o.coordinator.id = :coordinatorId")
    List<Organization> findAllByCoordinatorId(@Param("coordinatorId") Long coordinatorId);
}
