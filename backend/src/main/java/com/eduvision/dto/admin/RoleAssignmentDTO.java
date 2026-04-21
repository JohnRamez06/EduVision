package com.eduvision.dto.admin;

import jakarta.validation.constraints.NotBlank;

public class RoleAssignmentDTO {

    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "roleId is required")
    private String roleId;

    // Getters & Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getRoleId() { return roleId; }
    public void setRoleId(String roleId) { this.roleId = roleId; }
}