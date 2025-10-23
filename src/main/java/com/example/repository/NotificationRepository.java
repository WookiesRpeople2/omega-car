package com.example.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.example.model.Notification;

@Repository
public interface NotificationRepository extends BaseRepository<Notification, UUID> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
    void deleteByUserIdAndRelatedId(UUID userId, UUID relatedId);
    void deleteByRelatedId(UUID relatedId);
}


