package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentRequestDto {

    @NotNull(message = "Opportunity ID is required")
    private Long opportunityId;

    @Size(max = 500)
    private String notes;

    private Double hoursLogged;
}
