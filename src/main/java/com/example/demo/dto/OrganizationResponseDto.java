package com.example.demo.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationResponseDto {
    private Long id;
    private String name;
    private String mission;
    private String address;
    private String contactEmail;
    private String contactPhone;
    private Integer foundedYear;
    private String status;
    private Long coordinatorId;
    private String coordinatorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
