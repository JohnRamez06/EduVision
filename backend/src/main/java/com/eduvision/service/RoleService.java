package com.eduvision.service;

import com.eduvision.exception.ResourceNotFoundException;
import com.eduvision.model.*;
import com.eduvision.repository.PermissionRepository;
import com.eduvision.repository.RolePermissionRepository;
import com.eduvision.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class RoleService {

    private final RoleRepository           roleRepository;
    private final PermissionRepository     permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public RoleService(RoleRepository roleRepository,
                       PermissionRepository permissionRepository,
                       RolePermissionRepository rolePermissionRepository) {
        this.roleRepository           = roleRepository;
        this.permissionRepository     = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    // ─── GET ALL ROLES ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAllByActiveTrue();
    }

    // ─── GET PERMISSIONS FOR A ROLE ──────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Permission> getRolePermissions(String roleId) {
        findActiveRole(roleId); // validates the role exists
        return permissionRepository.findActiveByRoleId(roleId);
    }

    // ─── UPDATE PERMISSIONS FOR A ROLE ───────────────────────────────────────

    public List<Permission> updateRolePermissions(String roleId,
                                                  Set<String> permissionIds) {
        Role role = findActiveRole(roleId);

        // Remove old permissions
        rolePermissionRepository.deleteByRole(role);

        // Attach new permissions
        List<Permission> newPermissions =
                permissionRepository.findAllById(permissionIds);

        if (newPermissions.size() != permissionIds.size()) {
            throw new ResourceNotFoundException(
                "One or more permission IDs are invalid");
        }

        for (Permission permission : newPermissions) {
            RolePermissionId embeddedId = new RolePermissionId();
            embeddedId.setRoleId(role.getId());
            embeddedId.setPermissionId(permission.getId());

            RolePermission rp = new RolePermission();
            rp.setId(embeddedId);
            rp.setRole(role);
            rp.setPermission(permission);
            rp.setGrantedAt(LocalDateTime.now());

            rolePermissionRepository.save(rp);
        }

        role.setUpdatedAt(LocalDateTime.now());
        roleRepository.save(role);

        return newPermissions;
    }

    // ─── HELPER ──────────────────────────────────────────────────────────────

    private Role findActiveRole(String roleId) {
        return roleRepository.findByIdAndActiveTrue(roleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Role not found: " + roleId));
    }
}