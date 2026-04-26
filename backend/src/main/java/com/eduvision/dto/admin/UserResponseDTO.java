package com.eduvision.dto.admin;

import com.eduvision.model.User;
import com.eduvision.model.UserRole;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public class UserResponseDTO {

    private String id;
    private String email;
    private String fullName;
    private String profilePictureUrl;
    private Set<String> roles;    // role names e.g. ["LECTURER"]
    private boolean isActive;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;

    // Static factory — maps directly from User entity
    public static UserResponseDTO from(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.id                 = user.getId();
        dto.email              = user.getEmail();
        dto.fullName           = user.getFirstName() + " " + user.getLastName();
        dto.profilePictureUrl  = user.getProfilePictureUrl();
        dto.isActive  = user.getStatus() != null &&
                        user.getStatus().name().equalsIgnoreCase("ACTIVE");
        dto.lastLogin  = user.getLastLoginAt();
        dto.createdAt  = user.getCreatedAt();
        dto.roles = user.getUserRoles() == null ? Set.of() :
                user.getUserRoles().stream()
                    .filter(ur -> ur.getRole() != null)
                    .map(ur -> ur.getRole().getName())
                    .collect(Collectors.toSet());
        return dto;
    }

    // Getters
    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public Set<String> getRoles() { return roles; }
    public boolean isActive() { return isActive; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}