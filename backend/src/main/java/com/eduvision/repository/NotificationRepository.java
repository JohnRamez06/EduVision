package com.eduvision.repository;

import com.eduvision.model.Notification;
import com.eduvision.model.NotificationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByRecipient_IdAndStatus(String recipientId, NotificationStatus status);
    List<Notification> findByRecipient_IdAndStatusNot(String recipientId, NotificationStatus status);
}
