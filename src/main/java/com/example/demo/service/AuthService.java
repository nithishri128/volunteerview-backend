package com.example.demo.service;

import com.example.demo.dto.AuthRequestDto;
import com.example.demo.dto.AuthResponseDto;
import com.example.demo.dto.RefreshTokenRequestDto;
import com.example.demo.dto.RegisterDto;
import com.example.demo.entity.SystemUser;
import com.example.demo.exception.BusinessValidationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.SystemUserRepository;
import com.example.demo.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final SystemUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(SystemUserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponseDto register(RegisterDto dto) {

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BusinessValidationException(
                    "Username '" + dto.getUsername() + "' is already taken. Please choose a different username.");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessValidationException(
                    "Email '" + dto.getEmail() + "' is already registered.");
        }

        SystemUser.Role role;
        try {
            role = SystemUser.Role.valueOf(dto.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessValidationException(
                    "Invalid role: '" + dto.getRole() + "'. Allowed roles: PLATFORM_ADMIN, ORGANIZATION_COORDINATOR, VOLUNTEER");
        }

        SystemUser user = SystemUser.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .fullName(dto.getFullName())
                .phoneNumber(dto.getPhoneNumber() == null || dto.getPhoneNumber().isBlank() ? null : dto.getPhoneNumber())
                .role(role)
                .status(SystemUser.UserStatus.APPROVED)
                .isActive(true)
                .build();

        SystemUser saved = userRepository.save(user);

        return AuthResponseDto.builder()
                .username(saved.getUsername())
                .fullName(saved.getFullName())
                .role(saved.getRole().name())
                .status(saved.getStatus().name())
                .userId(saved.getId())
                .build();
    }

    @Transactional
    public AuthResponseDto login(AuthRequestDto dto) {

        SystemUser user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new BusinessValidationException("Invalid credentials. No account found."));

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new BusinessValidationException("Your account has been deactivated. Please contact a platform administrator.");
        }

        if (user.getRole() == SystemUser.Role.PLATFORM_ADMIN && user.getStatus() == SystemUser.UserStatus.PENDING) {
            user.setStatus(SystemUser.UserStatus.APPROVED);
            userRepository.save(user);
        }

        if (user.getStatus() != SystemUser.UserStatus.APPROVED) {
            throw new BusinessValidationException(
                    "Your account status is " + user.getStatus() + ". You can only log in once an administrator approves your account.");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new BusinessValidationException("Invalid credentials. Incorrect password.");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .userId(user.getId())
                .organizationId(user.getOrganization() != null ? user.getOrganization().getId() : null)
                .organizationName(user.getOrganization() != null ? user.getOrganization().getName() : null)
                .build();
    }

    @Transactional
    public AuthResponseDto refreshToken(RefreshTokenRequestDto dto) {

        if (dto.getRefreshToken() == null || dto.getRefreshToken().isBlank()) {
            throw new BusinessValidationException("Refresh token is required");
        }

        String username;
        try {
            username = jwtService.extractUsername(dto.getRefreshToken());
        } catch (Exception e) {
            throw new BusinessValidationException("Invalid or expired refresh token");
        }

        if (jwtService.isTokenExpired(dto.getRefreshToken())) {
            throw new BusinessValidationException("Refresh token has expired. Please log in again.");
        }

        SystemUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for refresh token"));

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new BusinessValidationException("Account is deactivated. Cannot refresh token.");
        }

        String newAccessToken = jwtService.generateAccessToken(user);

        return AuthResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(dto.getRefreshToken())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .userId(user.getId())
                .build();
    }
}
