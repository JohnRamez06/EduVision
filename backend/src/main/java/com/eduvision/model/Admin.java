package com.eduvision.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "admins")
public class Admin {

    @Id
    @Column(name = "user_id", columnDefinition = "char(36)")
    private String userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "department", length = 150)
    private String department;

    @Column(name = "access_level", nullable = false)
    private byte accessLevel;

    @Column(name = "can_manage_roles", nullable = false)
    private boolean canManageRoles;

    @Column(name = "notes")
    private String notes;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public byte getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(byte accessLevel) {
        this.accessLevel = accessLevel;
    }

    public boolean isCanManageRoles() {
        return canManageRoles;
    }

    public void setCanManageRoles(boolean canManageRoles) {
        this.canManageRoles = canManageRoles;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
