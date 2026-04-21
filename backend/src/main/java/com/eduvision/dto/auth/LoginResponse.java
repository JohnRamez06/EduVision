package com.eduvision.dto.auth;

import java.util.Set;

public class LoginResponse {

    private String      token;
    private String      email;
    private String      fullName;       // firstName + " " + lastName
    private Set<String> roles;          // Role names e.g. {"LECTURER"}
    private long        expiresIn;      // milliseconds (mirrors jwt.expiration property)

    public LoginResponse() {}

    public LoginResponse(String token, String email,
                          String fullName, Set<String> roles, long expiresIn) {
        this.token     = token;
        this.email     = email;
        this.fullName  = fullName;
        this.roles     = roles;
        this.expiresIn = expiresIn;
    }

    // Getters & Setters
    public String getToken()            { return token; }
    public void setToken(String token)  { this.token = token; }

    public String getEmail()            { return email; }
    public void setEmail(String email)  { this.email = email; }

    public String getFullName()               { return fullName; }
    public void setFullName(String fullName)  { this.fullName = fullName; }

    public Set<String> getRoles()             { return roles; }
    public void setRoles(Set<String> roles)   { this.roles = roles; }

    public long getExpiresIn()                { return expiresIn; }
    public void setExpiresIn(long expiresIn)  { this.expiresIn = expiresIn; }
}