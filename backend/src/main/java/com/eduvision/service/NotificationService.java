package com.eduvision.service;

import com.eduvision.dto.alert.NotificationDTO;
import com.eduvision.model.Notification;
import com.eduvision.model.NotificationChannel;
import com.eduvision.model.NotificationStatus;
import com.eduvision.model.Alert;
import com.eduvision.model.User;
import com.eduvision.repository.NotificationRepository;
import com.eduvision.repository.AlertRepository;
import com.eduvision.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private UserRepository userRepository;

    public Notification createNotification(String alertId, String recipientId, NotificationChannel channel, String subject, String body) {
        Notification notification = new Notification();
        notification.setId(java.util.UUID.randomUUID().toString());

        Optional<Alert> alert = alertRepository.findById(alertId);
        if (alert.isPresent()) {
            notification.setAlert(alert.get());
        }

        Optional<User> recipient = userRepository.findById(recipientId);
        if (recipient.isPresent()) {
            notification.setRecipient(recipient.get());
        }

        notification.setChannel(channel);
        notification.setStatus(NotificationStatus.pending);
        notification.setSubject(subject);
        notification.setBody(body);
        notification.setDeliveryAttempts((short) 0);
        notification.setCreatedAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    public boolean markAsRead(String notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setStatus(NotificationStatus.read);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
            return true;
        }
        return false;
    }

    public List<NotificationDTO> getUserNotifications(String userId) {
        List<Notification> notifications = notificationRepository.findByRecipient_IdAndStatusNot(userId, NotificationStatus.read);
        return notifications.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public boolean sendEmail(String notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            // TODO: Implement actual email sending logic
            // For now, just mark as sent
            notification.setStatus(NotificationStatus.sent);
            notification.setDeliveredAt(LocalDateTime.now());
            notification.setDeliveryAttempts((short) (notification.getDeliveryAttempts() + 1));
            notificationRepository.save(notification);
            return true;
        }
        return false;
    }

    public boolean sendPush(String notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            // TODO: Implement actual push notification logic
            // For now, just mark as delivered
            notification.setStatus(NotificationStatus.delivered);
            notification.setDeliveredAt(LocalDateTime.now());
            notification.setDeliveryAttempts((short) (notification.getDeliveryAttempts() + 1));
            notificationRepository.save(notification);
            return true;
        }
        return false;
    }

    private NotificationDTO convertToDTO(Notification notification) {
        return new NotificationDTO(
            notification.getId(),
            notification.getSubject(),
            notification.getBody(),
            notification.getChannel().toString(),
            notification.getStatus() == NotificationStatus.read,
            notification.getCreatedAt(),
            "/api/v1/alerts/" + (notification.getAlert() != null ? notification.getAlert().getId() : "")
        );
    }
}
