package com.eduvision.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SessionManagementService {

    private static final Logger logger = LoggerFactory.getLogger(SessionManagementService.class);
    private final JdbcTemplate jdbcTemplate;

    public SessionManagementService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Map<String, Object> startSession(Long courseId, String roomLocation, String cameraType, 
                                            LocalDateTime scheduledStart, LocalDateTime scheduledEnd, Long lecturerId) {
        String sessionId = UUID.randomUUID().toString();
        
        String sql = """
            INSERT INTO lecture_sessions (id, course_id, room_location, camera_type, scheduled_start, scheduled_end, 
                                         lecturer_id, status, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, 'active', NOW())
        """;
        
        jdbcTemplate.update(sql, sessionId, courseId, roomLocation, cameraType, scheduledStart, scheduledEnd, lecturerId);
        
        logger.info("Session started: {}", sessionId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("courseId", courseId);
        result.put("roomLocation", roomLocation);
        result.put("status", "active");
        result.put("startTime", scheduledStart.toString());
        
        return result;
    }

    @Transactional
    public void endSession(String sessionId) {
        String sql = "UPDATE lecture_sessions SET status = 'ended', actual_end = NOW() WHERE id = ?";
        jdbcTemplate.update(sql, sessionId);
        logger.info("Session ended: {}", sessionId);
    }

    public List<Map<String, Object>> getActiveSessions() {
        String sql = """
            SELECT ls.*, c.title as course_title 
            FROM lecture_sessions ls
            JOIN courses c ON c.id = ls.course_id
            WHERE ls.status = 'active'
            ORDER BY ls.scheduled_start DESC
        """;
        
        return jdbcTemplate.queryForList(sql);
    }

    public Map<String, Object> getSessionById(String sessionId) {
        String sql = """
            SELECT ls.*, c.title as course_title, c.code as course_code
            FROM lecture_sessions ls
            JOIN courses c ON c.id = ls.course_id
            WHERE ls.id = ?
        """;
        
        try {
            return jdbcTemplate.queryForMap(sql, sessionId);
        } catch (Exception e) {
            logger.error("Session not found: {}", sessionId);
            return null;
        }
    }
    
    public boolean isSessionActive(String sessionId) {
        String sql = "SELECT COUNT(*) FROM lecture_sessions WHERE id = ? AND status = 'active'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, sessionId);
        return count != null && count > 0;
    }
}