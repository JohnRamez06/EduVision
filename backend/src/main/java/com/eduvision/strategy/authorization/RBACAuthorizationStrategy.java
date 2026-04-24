package com.eduvision.strategy.authorization;

import com.eduvision.model.Role;
import com.eduvision.model.User;
import com.eduvision.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component("RBACAuthorizationStrategy")
public class RBACAuthorizationStrategy implements AuthorizationStrategy {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean hasPermission(String userId, String resource, String action) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) return false;

        User user = userOpt.get();
        Set<Role> roles = user.getUserRoles().stream()
            .map(userRole -> userRole.getRole())
            .collect(java.util.stream.Collectors.toSet());

        for (Role role : roles) {
            if (checkRolePermission(role, resource, action)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkRolePermission(Role role, String resource, String action) {
        // Simple hardcoded permissions for demo
        String roleName = role.getName();
        switch (roleName) {
            case "ADMIN":
                return true; // admin can do anything
            case "LECTURER":
                return resource.startsWith("session") && (action.equals("read") || action.equals("write"));
            case "STUDENT":
                return resource.startsWith("student") && action.equals("read");
            default:
                return false;
        }
    }
}