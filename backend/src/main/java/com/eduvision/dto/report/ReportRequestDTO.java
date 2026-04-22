package com.eduvision.dto.report;

import com.eduvision.model.ReportType;
import java.time.LocalDateTime;
import java.util.List;

public class ReportRequestDTO {

    private ReportType type;
    private String title;
    private String description;
    private String requestedById;
    private String courseId;
    private String sessionId;
    private String studentId;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private String scriptPath;
    private List<String> scriptArgs;

    public ReportType getType() { return type; }
    public void setType(ReportType type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRequestedById() { return requestedById; }
    public void setRequestedById(String requestedById) { this.requestedById = requestedById; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public LocalDateTime getDateFrom() { return dateFrom; }
    public void setDateFrom(LocalDateTime dateFrom) { this.dateFrom = dateFrom; }
    public LocalDateTime getDateTo() { return dateTo; }
    public void setDateTo(LocalDateTime dateTo) { this.dateTo = dateTo; }
    public String getScriptPath() { return scriptPath; }
    public void setScriptPath(String scriptPath) { this.scriptPath = scriptPath; }
    public List<String> getScriptArgs() { return scriptArgs; }
    public void setScriptArgs(List<String> scriptArgs) { this.scriptArgs = scriptArgs; }
}
