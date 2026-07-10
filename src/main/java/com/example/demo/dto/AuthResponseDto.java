package com.example.demo.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDto {
    private String accessToken;
    private String refreshToken;
    private String username;
    private String fullName;
    private String role;
    private String status;
    private Long userId;
    private Long organizationId;
    private String organizationName;
}
