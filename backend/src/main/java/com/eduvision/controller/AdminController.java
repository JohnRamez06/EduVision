package com.eduvision.controller;

import com.eduvision.dto.admin.*;
import com.eduvision.model.Permission;
import com.eduvision.model.Role;
import com.eduvision.service.AdminService;
import com.eduvision.service.PermissionService;
import com.eduvision.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")          // entire controller is admin-only
public class AdminController {

    private final AdminService      adminService;
    private final RoleService       roleService;
    private final PermissionService permissionService;

    public AdminController(AdminService adminService, RoleService roleService,
                           PermissionService permissionService) {
        this.adminService      = adminService;
        this.roleService       = roleService;
        this.permissionService = permissionService;
    }

    // ─── USER ENDPOINTS ──────────────────────────────────────────────────────

    /** GET /api/v1/admin/users — list all non-deleted users */
    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    /** GET /api/v1/admin/users/{id} — get single user */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    /** POST /api/v1/admin/users — create user + assign role */
    @PostMapping("/users")
    public ResponseEntity<UserResponseDTO> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        UserResponseDTO created = adminService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** PUT /api/v1/admin/users/{id} — update name / role / active status */
    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable String id,
            @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(adminService.updateUser(id, request));
    }

    /** DELETE /api/v1/admin/users/{id} — soft delete */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();   // 204
    }

    /** POST /api/v1/admin/users/{id}/picture — upload profile photo */
    @PostMapping(value = "/users/{id}/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponseDTO> uploadPicture(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(adminService.uploadProfilePicture(id, file));
    }

    // ─── ROLE ENDPOINTS ──────────────────────────────────────────────────────

    /** GET /api/v1/admin/permissions — list all active permissions */
    @GetMapping("/permissions")
    public ResponseEntity<List<Permission>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    /** GET /api/v1/admin/roles — list all active roles */
    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    /** GET /api/v1/admin/roles/{roleId}/permissions — get role's permissions */
    @GetMapping("/roles/{roleId}/permissions")
    public ResponseEntity<List<Permission>> getRolePermissions(
            @PathVariable String roleId) {
        return ResponseEntity.ok(roleService.getRolePermissions(roleId));
    }

    /** PUT /api/v1/admin/roles/{roleId}/permissions — replace permissions */
    @PutMapping("/roles/{roleId}/permissions")
    public ResponseEntity<List<Permission>> updateRolePermissions(
            @PathVariable String roleId,
            @RequestBody Set<String> permissionIds) {
        return ResponseEntity.ok(
                roleService.updateRolePermissions(roleId, permissionIds));
    }

    /** POST /api/v1/admin/roles/assign — assign a role to a user */
    @PostMapping("/roles/assign")
    public ResponseEntity<UserResponseDTO> assignRole(
            @Valid @RequestBody RoleAssignmentDTO dto) {
        return ResponseEntity.ok(adminService.assignRole(dto));
    }
}