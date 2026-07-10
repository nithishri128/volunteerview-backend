package com.example.demo.service;

import com.example.demo.dto.UserResponseDto;
import com.example.demo.dto.UserUpdateDto;
import com.example.demo.entity.Organization;
import com.example.demo.entity.SystemUser;
import com.example.demo.exception.BusinessValidationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.OrganizationRepository;
import com.example.demo.repository.SystemUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final SystemUserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    public UserService(SystemUserRepository userRepository, OrganizationRepository organizationRepository) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        SystemUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return mapToResponseDto(user);
    }

    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateDto dto) {
        SystemUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (!user.getEmail().equalsIgnoreCase(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessValidationException("Email '" + dto.getEmail() + "' is already in use.");
        }

        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());

        SystemUser saved = userRepository.save(user);
        return mapToResponseDto(saved);
    }

    @Transactional
    public void deleteUser(Long id) {
        SystemUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (user.getRole() == SystemUser.Role.PLATFORM_ADMIN
                && userRepository.countByRole(SystemUser.Role.PLATFORM_ADMIN) <= 1) {
            throw new BusinessValidationException("Cannot delete the last remaining PLATFORM_ADMIN account.");
        }

        userRepository.delete(user);
    }

    @Transactional
    public UserResponseDto updateStatus(Long id, String statusStr) {
        SystemUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        user.setStatus(SystemUser.UserStatus.valueOf(statusStr.toUpperCase()));

        SystemUser saved = userRepository.save(user);
        return mapToResponseDto(saved);
    }

    @Transactional
    public UserResponseDto assignOrganization(Long userId, Long orgId) {
        SystemUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", orgId));

        user.setOrganization(organization);

        SystemUser saved = userRepository.save(user);
        return mapToResponseDto(saved);
    }

    private UserResponseDto mapToResponseDto(SystemUser user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .organizationId(user.getOrganization() != null ? user.getOrganization().getId() : null)
                .organizationName(user.getOrganization() != null ? user.getOrganization().getName() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
