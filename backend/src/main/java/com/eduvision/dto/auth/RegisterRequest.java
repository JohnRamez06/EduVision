package com.eduvision.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    // ── Required for all roles ────────────────────────────────────────────
    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank(message = "roleName is required: STUDENT | LECTURER | ADMIN")
    private String roleName;               // "STUDENT" | "LECTURER" | "ADMIN"

    // ── Student-specific (required when roleName = STUDENT) ───────────────
    private String studentNumber;
    private String program;
    private Byte   yearOfStudy;

    // ── Lecturer-specific (required when roleName = LECTURER) ─────────────
    private String employeeId;
    private String department;
    private String specialization;

    // ── Admin-specific (required when roleName = ADMIN) ───────────────────
    private Byte    accessLevel;           // defaults to 1 if null
    private Boolean canManageRoles;        // defaults to false if null

    // Getters & Setters
    public String getEmail()                   { return email; }
    public void setEmail(String email)         { this.email = email; }

    public String getPassword()                { return password; }
    public void setPassword(String p)          { this.password = p; }

    public String getFirstName()               { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName()                { return lastName; }
    public void setLastName(String lastName)   { this.lastName = lastName; }

    public String getRoleName()                { return roleName; }
    public void setRoleName(String roleName)   { this.roleName = roleName; }

    public String getStudentNumber()                   { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

    public String getProgram()                 { return program; }
    public void setProgram(String program)     { this.program = program; }

    public Byte getYearOfStudy()               { return yearOfStudy; }
    public void setYearOfStudy(Byte y)         { this.yearOfStudy = y; }

    public String getEmployeeId()              { return employeeId; }
    public void setEmployeeId(String e)        { this.employeeId = e; }

    public String getDepartment()              { return department; }
    public void setDepartment(String d)        { this.department = d; }

    public String getSpecialization()          { return specialization; }
    public void setSpecialization(String s)    { this.specialization = s; }

    public Byte getAccessLevel()               { return accessLevel; }
    public void setAccessLevel(Byte a)         { this.accessLevel = a; }

    public Boolean getCanManageRoles()         { return canManageRoles; }
    public void setCanManageRoles(Boolean c)   { this.canManageRoles = c; }
}