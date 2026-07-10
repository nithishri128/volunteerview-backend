package com.example.demo.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImpactSummaryDto {
    private Double totalHoursContributed;
    private Integer totalBeneficiariesServed;
    private Double averageRating;
    private Long totalReports;
}
