package com.eduvision.service;

import com.eduvision.dto.auth.LoginRequest;
import com.eduvision.dto.auth.LoginResponse;
import com.eduvision.dto.auth.RegisterRequest;
import com.eduvision.exception.ResourceNotFoundException;
import com.eduvision.exception.UnauthorizedException;
import com.eduvision.model.*;
import com.eduvision.repository.*;
import com.eduvision.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {

    private final UserRepository     userRepository;
    private final RoleRepository     roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final StudentRepository  studentRepository;
    private final LecturerRepository lecturerRepository;
    private final AdminRepository    adminRepository;
    private final PasswordEncoder    passwordEncoder;
    private final JwtService         jwtService;

    @Value("${eduvision.jwt.expiration}")
    private long jwtExpirationMs;

    public AuthService(UserRepository userRepository,
                        RoleRepository roleRepository,
                        UserRoleRepository userRoleRepository,
                        StudentRepository studentRepository,
                        LecturerRepository lecturerRepository,
                        AdminRepository adminRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.userRepository     = userRepository;
        this.roleRepository     = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.studentRepository  = studentRepository;
        this.lecturerRepository = lecturerRepository;
        this.adminRepository    = adminRepository;
        this.passwordEncoder    = passwordEncoder;
        this.jwtService         = jwtService;
    }

    // ─── LOGIN ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public LoginResponse authenticate(LoginRequest request) {
        // 1. Find user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException(
                        "Invalid email or password"));

        // 2. Soft-deleted or inactive guard
        if (user.getDeletedAt() != null) {
            throw new UnauthorizedException("Account no longer exists");
        }
        if (UserStatus.suspended.equals(user.getStatus())) {
            throw new UnauthorizedException("Account is suspended");
        }

        // 3. Password check
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        // 4. Generate token (subject = email, matches JwtAuthenticationFilter)
        String token = jwtService.generateToken(user.getEmail());

        return buildResponse(user, token);
    }

    // ─── REGISTER ─────────────────────────────────────────────────────────

    public LoginResponse register(RegisterRequest req) {
        // 1. Duplicate email guard
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException(
                    "Email already registered: " + req.getEmail());
        }

        // 2. Resolve role
        Role role = roleRepository.findByName(req.getRoleName().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found: " + req.getRoleName()));

        // 3. Build and persist User
        User user = buildUser(req);
        User savedUser = userRepository.save(user);

        // 4. Assign role via UserRole join entity
        assignRole(savedUser, role);

        // 5. Create role-specific profile
        switch (req.getRoleName().toUpperCase()) {
            case "STUDENT"  -> createStudentProfile(savedUser, req);
            case "LECTURER" -> createLecturerProfile(savedUser, req);
            case "ADMIN"    -> createAdminProfile(savedUser, req);
            default         -> { /* no profile needed for unknown roles */ }
        }

        // 6. Generate JWT and return
        String token = jwtService.generateToken(savedUser.getEmail());
        return buildResponse(savedUser, token);
    }

    // ─── PRIVATE BUILDERS ─────────────────────────────────────────────────

    private User buildUser(RegisterRequest req) {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setStatus(UserStatus.active);
        user.setLocale("en");
        user.setTimezone("UTC");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    private void assignRole(User user, Role role) {
        UserRoleId embeddedId = new UserRoleId();
        embeddedId.setUserId(user.getId());
        embeddedId.setRoleId(role.getId());

        UserRole userRole = new UserRole();
        userRole.setId(embeddedId);
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setAssignedAt(LocalDateTime.now());

        userRoleRepository.save(userRole);
    }

    private void createStudentProfile(User user, RegisterRequest req) {
        Student student = new Student();
        student.setUser(user);
        student.setStudentNumber(
                req.getStudentNumber() != null
                        ? req.getStudentNumber()
                        : "STU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        student.setProgram(req.getProgram());
        student.setYearOfStudy(req.getYearOfStudy() != null ? req.getYearOfStudy() : (byte) 1);
        student.setEnrolledAt(LocalDate.now());
        studentRepository.save(student);
    }

    private void createLecturerProfile(User user, RegisterRequest req) {
        Lecturer lecturer = new Lecturer();
        lecturer.setUser(user);
        lecturer.setEmployeeId(
                req.getEmployeeId() != null
                        ? req.getEmployeeId()
                        : "EMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        lecturer.setDepartment(req.getDepartment());
        lecturer.setSpecialization(req.getSpecialization());
        lecturer.setHiredAt(LocalDate.now());
        lecturerRepository.save(lecturer);
    }

    private void createAdminProfile(User user, RegisterRequest req) {
        Admin admin = new Admin();
        admin.setUser(user);
        admin.setAccessLevel(req.getAccessLevel() != null ? req.getAccessLevel() : (byte) 1);
        admin.setCanManageRoles(Boolean.TRUE.equals(req.getCanManageRoles()));
        adminRepository.save(admin);
    }

    private LoginResponse buildResponse(User user, String token) {
        Set<String> roles = user.getUserRoles() == null ? Set.of() :
                user.getUserRoles().stream()
                        .filter(ur -> ur.getRole() != null)
                        .map(ur -> ur.getRole().getName())
                        .collect(Collectors.toSet());

        return new LoginResponse(
                token,
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName(),
                roles,
                jwtExpirationMs);
    }
}