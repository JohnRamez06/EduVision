package com.eduvision.security;

import com.eduvision.model.User;
import com.eduvision.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Called by JwtAuthenticationFilter with the email (JWT subject).
     * Returns a UserDetails whose authorities are the real Role names.
     */
    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + email));

        // Map each Role name → "ROLE_{NAME}" for Spring Security's hasRole() checks
        List<SimpleGrantedAuthority> authorities = user.getUserRoles() == null
                ? List.of()
                : user.getUserRoles().stream()
                        .filter(ur -> ur.getRole() != null && ur.getRole().isActive())
                        .map(ur -> new SimpleGrantedAuthority(
                                "ROLE_" + ur.getRole().getName().toUpperCase()))
                        .collect(Collectors.toList());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(user.getLockedUntil() != null &&
                        user.getLockedUntil().isAfter(java.time.LocalDateTime.now()))
                .credentialsExpired(false)
                .disabled(!com.eduvision.model.UserStatus.active.equals(user.getStatus()))
                .build();
    }
}