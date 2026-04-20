package com.eduvision.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface PermissionRepository extends JpaRepository<Object, String> {
    // TODO: add query methods
}
