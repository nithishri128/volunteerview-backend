package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpportunityRequestDto {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    @Size(max = 255)
    private String location;

    @Size(max = 50)
    private String category;

    /** Format: YYYY-MM-DD */
    @NotBlank
    private String scheduledDate;

    /** Format: HH:mm */
    @NotBlank
    private String startTime;

    /** Format: HH:mm */
    @NotBlank
    private String endTime;

    @NotNull
    @Min(1)
    private Integer maxCapacity;

    @Size(max = 500)
    private String requiredSkills;

    @NotNull
    private Long organizationId;
}
