package com.example.demo.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImpactReportResponseDto {
    private Long id;
    private Long enrollmentId;
    private Long volunteerId;
    private String volunteerName;
    private String opportunityTitle;
    private String organizationName;
    private String summary;
    private Double hoursContributed;
    private Integer beneficiariesServed;
    private Integer rating;
    private LocalDateTime submittedAt;
}
