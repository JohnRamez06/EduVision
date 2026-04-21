package com.eduvision.repository;

import com.eduvision.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

    Optional<Role> findByName(String name);                   // used by register + AdminService
    Optional<Role> findByIdAndActiveTrue(String id);          // used by AdminService
    List<Role>     findAllByActiveTrue();                      // used by RoleService
    boolean        existsByName(String name);
}