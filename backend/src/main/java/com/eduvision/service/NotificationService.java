// src/main/java/com/eduvision/service/NotificationService.java
package com.eduvision.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class NotificationService {

    private final JdbcTemplate jdbc;

    public NotificationService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> getAllNotifications(String email) {
        return jdbc.queryForList(
                "SELECT n.notification_id, n.type, n.title, n.message, " +
                "       n.status, n.link, n.created_at, n.read_at " +
                "FROM notifications n JOIN users u ON u.user_id = n.user_id " +
                "WHERE u.email = ? ORDER BY n.created_at DESC",
                email);
    }

    public List<Map<String, Object>> getUnreadNotifications(String email) {
        return jdbc.queryForList(
                "SELECT n.notification_id, n.type, n.title, n.message, " +
                "       n.status, n.link, n.created_at " +
                "FROM notifications n JOIN users u ON u.user_id = n.user_id " +
                "WHERE u.email = ? AND n.status = 'unread' ORDER BY n.created_at DESC",
                email);
    }

    @Transactional
    public void markAsRead(String notificationId) {
        jdbc.update(
                "UPDATE notifications SET status = 'read', read_at = ? WHERE notification_id = ?",
                LocalDateTime.now(), notificationId);
    }

    @Transactional
    public void deleteNotification(String notificationId) {
        jdbc.update("DELETE FROM notifications WHERE notification_id = ?", notificationId);
    }
}