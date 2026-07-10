package com.example.demo.repository;

import com.example.demo.entity.SystemUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SystemUserRepository extends JpaRepository<SystemUser, Long> {

    Optional<SystemUser> findByUsername(String username);

    Optional<SystemUser> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<SystemUser> findByRole(SystemUser.Role role);

    List<SystemUser> findByStatus(SystemUser.UserStatus status);

    List<SystemUser> findByIsActiveTrue();

    long countByRole(SystemUser.Role role);
}
