package com.eduvision.service;

import com.eduvision.dto.admin.*;
import com.eduvision.exception.ResourceNotFoundException;
import com.eduvision.model.*;
import com.eduvision.repository.RoleRepository;
import com.eduvision.repository.UserRepository;
import com.eduvision.repository.UserRoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {

    private final UserRepository     userRepository;
    private final RoleRepository     roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder    passwordEncoder;

    public AdminService(UserRepository userRepository,
                        RoleRepository roleRepository,
                        UserRoleRepository userRoleRepository,
                        PasswordEncoder passwordEncoder) {
        this.userRepository     = userRepository;
        this.roleRepository     = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder    = passwordEncoder;
    }

    // ─── READ ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAllByDeletedAtIsNull()
                .stream()
                .map(UserResponseDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(String id) {
        User user = findActiveUserById(id);
        return UserResponseDTO.from(user);
    }

    // ─── CREATE ──────────────────────────────────────────────────────────────

    public UserResponseDTO createUser(UserCreateRequest req) {
        // 1. Duplicate email check
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException(
                "Email already in use: " + req.getEmail());
        }

        // 2. Resolve role
        Role role = roleRepository.findByName(req.getRoleName())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Role not found: " + req.getRoleName()));

        // 3. Build User
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setStatus(UserStatus.active);
        user.setLocale("en");
        user.setTimezone("UTC");
        user.setProfilePictureUrl(req.getProfilePictureUrl());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // 4. Assign role via UserRole join entity
        attachRole(savedUser, role);

        return UserResponseDTO.from(savedUser);
    }

    // ─── UPDATE ──────────────────────────────────────────────────────────────

    public UserResponseDTO updateUser(String id, UserUpdateRequest req) {
        User user = findActiveUserById(id);

        if (req.getFirstName()         != null) user.setFirstName(req.getFirstName());
        if (req.getLastName()          != null) user.setLastName(req.getLastName());
        if (req.getProfilePictureUrl() != null) user.setProfilePictureUrl(req.getProfilePictureUrl());

        if (req.getIsActive() != null) {
            user.setStatus(req.getIsActive()
                    ? UserStatus.active
                    : UserStatus.inactive);
        }

        if (req.getRoleName() != null) {
            Role newRole = roleRepository.findByName(req.getRoleName())
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found: " + req.getRoleName()));
            // Remove existing roles then attach new one
            userRoleRepository.deleteByUser(user);
            attachRole(user, newRole);
        }

        user.setUpdatedAt(LocalDateTime.now());
        return UserResponseDTO.from(userRepository.save(user));
    }

    // ─── DELETE (soft) ───────────────────────────────────────────────────────

    public void deleteUser(String id) {
        User user = findActiveUserById(id);
        user.setDeletedAt(LocalDateTime.now());
        user.setStatus(UserStatus.inactive);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // ─── PROFILE PICTURE ─────────────────────────────────────────────────────

    public UserResponseDTO uploadProfilePicture(String id, MultipartFile file) throws IOException {
        User user = findActiveUserById(id);

        // Derive a safe file extension from the original filename or content type
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        String ext = originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.'))
                : ".jpg";

        // Store under uploads/profile-pictures/ using userId as the stable filename
        // (overwrites previous picture automatically on re-upload)
        Path dir = Paths.get("uploads", "profile-pictures");
        Files.createDirectories(dir);
        Path target = dir.resolve(user.getId() + ext);
        file.transferTo(target.toAbsolutePath().toFile());

        user.setProfilePictureUrl("/uploads/profile-pictures/" + user.getId() + ext);
        user.setUpdatedAt(LocalDateTime.now());
        return UserResponseDTO.from(userRepository.save(user));
    }

    // ─── ROLE ASSIGNMENT ─────────────────────────────────────────────────────

    public UserResponseDTO assignRole(RoleAssignmentDTO dto) {
        User user = findActiveUserById(dto.getUserId());
        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Role not found: " + dto.getRoleId()));

        // Avoid duplicate assignment
        boolean alreadyAssigned = user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getId().equals(role.getId()));

        if (!alreadyAssigned) {
            attachRole(user, role);
        }

        return UserResponseDTO.from(userRepository.findById(user.getId())
                .orElseThrow());
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private User findActiveUserById(String id) {
        return userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "User not found: " + id));
    }

    private void attachRole(User user, Role role) {
        UserRoleId embeddedId = new UserRoleId();
        embeddedId.setUserId(user.getId());
        embeddedId.setRoleId(role.getId());

        UserRole userRole = new UserRole();
        userRole.setId(embeddedId);
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setAssignedAt(LocalDateTime.now());

        userRoleRepository.save(userRole);
    }
}