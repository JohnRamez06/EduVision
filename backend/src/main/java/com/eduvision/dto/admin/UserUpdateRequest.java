package com.eduvision.dto.admin;

public class UserUpdateRequest {

    private String firstName;
    private String lastName;
    private String roleName;    // new role to assign; null means no role change
    private Boolean isActive;   // true = active, false = inactive/suspended

    // Getters & Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}