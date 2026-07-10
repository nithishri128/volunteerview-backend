package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "volunteer_enrollments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"volunteer_id", "opportunity_id"}, name = "uk_volunteer_opportunity")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VolunteerEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "volunteer_id", nullable = false)
    private SystemUser volunteer;

    @ManyToOne
    @JoinColumn(name = "opportunity_id", nullable = false)
    private Opportunity opportunity;

    @Enumerated(EnumType.STRING)
    @Column(name = "enrollment_status", nullable = true, length = 30)
    private EnrollmentStatus status;

    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private LocalDateTime enrolledAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "hours_logged")
    private Double hoursLogged;

    @Column(length = 500)
    private String notes;

    @PrePersist
    protected void onCreate() {
        this.enrolledAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = EnrollmentStatus.PENDING;
        }
        if (this.hoursLogged == null) {
            this.hoursLogged = 0.0;
        }
    }

    public enum EnrollmentStatus {
        PENDING, APPROVED, STARTED, COMPLETED, REJECTED, CANCELLED
    }
}
