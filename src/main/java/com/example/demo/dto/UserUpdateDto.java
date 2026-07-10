package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDto {

    @NotBlank
    @Size(max = 100)
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @Size(max = 20)
    private String phoneNumber;
}
