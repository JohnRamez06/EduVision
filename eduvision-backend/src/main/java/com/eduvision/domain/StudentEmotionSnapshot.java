package com.eduvision.domain;
import jakarta.persistence.*;
@Entity
@Table(name = "StudentEmotionSnapshot")
public class StudentEmotionSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    // TODO: add fields from schema
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}
