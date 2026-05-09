// src/main/java/com/eduvision/config/SecurityConfig.java
package com.eduvision.config;

import com.eduvision.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                // PUBLIC ENDPOINTS (no authentication needed)
                .requestMatchers(
                        "/api/v1/auth/**",
                        "/ws/**",
                        "/actuator/health",
                        "/api/v1/reports/download/**",  // public
                        "/api/v1/html-reports/**"       // public — IDs in URL, no JWT needed
                ).permitAll()
                // 🔥 Allow Python to POST emotion data and attendance
                .requestMatchers(HttpMethod.POST, "/api/v1/emotion-data/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/attendance/record").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/attendance/weekly/calculate").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/attendance/**").permitAll()
                // 🔥 ADD THIS - Allow Python to POST alerts (no authentication required)
                .requestMatchers(HttpMethod.POST, "/api/v1/alerts/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/alerts/**").permitAll()
                // DEAN ENDPOINTS
                .requestMatchers("/api/v1/dean/**").hasRole("DEAN")
                .requestMatchers("/api/v1/facade/dean/**").hasRole("DEAN")
                // ADMIN ENDPOINTS
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/face-enrollment/**").hasRole("ADMIN")
                // LECTURER ENDPOINTS
                .requestMatchers("/api/v1/lecturer/**").hasRole("LECTURER")
                .requestMatchers("/api/v1/facade/lecturer/**").hasRole("LECTURER")
                .requestMatchers(HttpMethod.POST, "/api/v1/sessions/start").hasRole("LECTURER")
                .requestMatchers(HttpMethod.POST, "/api/v1/sessions/*/end").hasRole("LECTURER")
                // STUDENT ENDPOINTS
                .requestMatchers("/api/v1/student/**").hasRole("STUDENT")
                .requestMatchers("/api/v1/facade/student/**").hasRole("STUDENT")
                .requestMatchers("/api/v1/consent/**").hasRole("STUDENT")
                // SHARED ENDPOINTS — reports accessible by all authenticated roles
                .requestMatchers("/api/v1/html-reports/**").hasAnyRole("STUDENT", "LECTURER", "DEAN", "ADMIN")
                .requestMatchers("/api/v1/reports/my/**").hasAnyRole("STUDENT", "LECTURER", "DEAN", "ADMIN")
                .requestMatchers("/api/v1/reports/my").hasAnyRole("STUDENT", "LECTURER", "DEAN", "ADMIN")
                .requestMatchers("/api/v1/reports/**").hasAnyRole("STUDENT", "LECTURER", "DEAN", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/emotion-data/**").hasAnyRole("LECTURER", "DEAN", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/sessions/**").hasAnyRole("LECTURER", "DEAN", "ADMIN")
                .requestMatchers("/api/v1/attendance/**").permitAll()
                .requestMatchers("/api/v1/camera/**").hasRole("ADMIN")
                // Everything else requires authentication
                .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "http://localhost:8000"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        source.registerCorsConfiguration("/api/v1/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
