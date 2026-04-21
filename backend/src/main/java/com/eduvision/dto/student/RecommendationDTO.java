package com.eduvision.dto.student;

public class RecommendationDTO {

    public enum Priority { HIGH, MEDIUM, LOW }

    private String   type;        // "FOCUS" | "DISTRACTION" | "COMPREHENSION" | "WELLBEING" | "ATTENDANCE" | "POSITIVE" | "ONBOARDING"
    private String   title;
    private String   description;
    private Priority priority;

    public RecommendationDTO() {}

    public RecommendationDTO(String type, String title,
                              String description, Priority priority) {
        this.type        = type;
        this.title       = title;
        this.description = description;
        this.priority    = priority;
    }

    // Getters & Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
}