package com.eduvision.repository;

import com.eduvision.model.Role;
import com.eduvision.model.RolePermission;
import com.eduvision.model.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {

    void deleteByRole(Role role);
}
