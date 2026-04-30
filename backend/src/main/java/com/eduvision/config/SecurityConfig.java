package com.eduvision.config;

import com.eduvision.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ──────────────────────────────────────────────
                // PUBLIC ENDPOINTS (no authentication needed)
                // ──────────────────────────────────────────────
                .requestMatchers(
                        "/api/v1/auth/**",          // Login, register, logout
                        "/ws/**",                    // WebSocket handshake
                        "/actuator/health"           // Health check
                ).permitAll()

                // ──────────────────────────────────────────────
                // DEAN ENDPOINTS (only DEAN role)
                // ──────────────────────────────────────────────
                .requestMatchers("/api/v1/dean/**").hasRole("DEAN")
                .requestMatchers("/api/v1/facade/dean/**").hasRole("DEAN")

                // ──────────────────────────────────────────────
                // ADMIN ENDPOINTS (only ADMIN role)
                // ──────────────────────────────────────────────
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/face-enrollment/**").hasRole("ADMIN")

                // ──────────────────────────────────────────────
                // LECTURER ENDPOINTS (only LECTURER role)
                // ──────────────────────────────────────────────
                .requestMatchers("/api/v1/lecturer/**").hasRole("LECTURER")
                .requestMatchers("/api/v1/facade/lecturer/**").hasRole("LECTURER")
                .requestMatchers(HttpMethod.POST, "/api/v1/sessions/start").hasRole("LECTURER")
                .requestMatchers(HttpMethod.POST, "/api/v1/sessions/*/end").hasRole("LECTURER")

                // ──────────────────────────────────────────────
                // STUDENT ENDPOINTS (only STUDENT role)
                // ──────────────────────────────────────────────
                .requestMatchers("/api/v1/student/**").hasRole("STUDENT")
                .requestMatchers("/api/v1/facade/student/**").hasRole("STUDENT")
                .requestMatchers("/api/v1/consent/**").hasRole("STUDENT")

                // ──────────────────────────────────────────────
                // SHARED ENDPOINTS (multiple roles)
                // ──────────────────────────────────────────────
                // Reports: STUDENT, LECTURER, DEAN, ADMIN can access
                .requestMatchers("/api/v1/reports/**").hasAnyRole("STUDENT", "LECTURER", "DEAN", "ADMIN")
                
                // Emotion data: LECTURER, DEAN, ADMIN can view
                .requestMatchers(HttpMethod.GET, "/api/v1/emotion-data/**").hasAnyRole("LECTURER", "DEAN", "ADMIN")
                
                // Sessions (view): LECTURER, DEAN, ADMIN
                .requestMatchers(HttpMethod.GET, "/api/v1/sessions/**").hasAnyRole("LECTURER", "DEAN", "ADMIN")
                
                // Alerts: LECTURER, DEAN, ADMIN
                .requestMatchers("/api/v1/alerts/**").hasAnyRole("LECTURER", "DEAN", "ADMIN")
                
                // Notifications: all authenticated users
                .requestMatchers("/api/v1/notifications/**").authenticated()
                
                // Attendance: LECTURER, DEAN, ADMIN, STUDENT (own data only)
                .requestMatchers("/api/v1/attendance/**").hasAnyRole("STUDENT", "LECTURER", "DEAN", "ADMIN")
                
                // Camera configs: ADMIN only
                .requestMatchers("/api/v1/camera/**").hasRole("ADMIN")

                // ──────────────────────────────────────────────
                // EVERYTHING ELSE requires authentication
                // ──────────────────────────────────────────────
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}