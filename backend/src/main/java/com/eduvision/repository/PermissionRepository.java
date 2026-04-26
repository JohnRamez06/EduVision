package com.eduvision.repository;

import com.eduvision.model.Permission;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {

    @Query("SELECT p FROM RolePermission rp JOIN rp.permission p WHERE rp.role.id = :roleId AND p.active = true")
    List<Permission> findActiveByRoleId(@Param("roleId") String roleId);
}
