package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "impact_reports",
        uniqueConstraints = @UniqueConstraint(columnNames = {"enrollment_id"}, name = "uk_enrollment_report")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImpactReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false, unique = true)
    private VolunteerEnrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_id", nullable = false)
    private SystemUser volunteer;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(nullable = false)
    private Double hoursContributed;

    @Column(nullable = false)
    private Integer beneficiariesServed;

    @Column(nullable = false)
    private Integer rating;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @PrePersist
    protected void onCreate() {
        this.submittedAt = LocalDateTime.now();
    }
}
