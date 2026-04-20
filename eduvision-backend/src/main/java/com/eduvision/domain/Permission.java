package com.eduvision.domain;
import jakarta.persistence.*;
@Entity
@Table(name = "Permission")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    // TODO: add fields from schema
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}
