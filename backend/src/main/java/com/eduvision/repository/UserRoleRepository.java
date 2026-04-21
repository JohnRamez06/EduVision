package com.eduvision.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eduvision.model.User;
import com.eduvision.model.UserRole;
import com.eduvision.model.UserRoleId;


@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    void deleteByUser(User user);
}
