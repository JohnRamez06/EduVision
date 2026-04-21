package com.eduvision.repository;

import com.eduvision.model.User;
import com.eduvision.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // ── Auth ──────────────────────────────────────────────────────────────
    Optional<User> findByEmail(String email);
    boolean        existsByEmail(String email);

    // ── Admin queries ─────────────────────────────────────────────────────
    Optional<User> findByIdAndDeletedAtIsNull(String id);
    List<User>     findAllByDeletedAtIsNull();

    // ── Status filter ─────────────────────────────────────────────────────
    List<User>     findAllByStatus(UserStatus status);
}