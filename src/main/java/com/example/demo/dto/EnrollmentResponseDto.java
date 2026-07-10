package com.example.demo.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentResponseDto {
    private Long id;
    private Long volunteerId;
    private String volunteerName;
    private Long opportunityId;
    private String opportunityTitle;
    private String organizationName;
    private String status;
    private LocalDateTime enrolledAt;
    private LocalDateTime approvedAt;
    private Double hoursLogged;
    private String notes;
}
