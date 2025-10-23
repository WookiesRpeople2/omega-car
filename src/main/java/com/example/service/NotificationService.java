package com.example.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.model.Notification;
import com.example.model.NotificationType;
import com.example.repository.NotificationRepository;

@Service
public class NotificationService {
    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Notification create(UUID userId, NotificationType type, String title, String message, UUID relatedId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setRelatedId(relatedId);
        n.setRead(false);
        return repository.save(n);
    }

    @Transactional(readOnly = true)
    public List<Notification> listForUser(UUID userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void markRead(UUID id) {
        repository.findById(id).ifPresent(n -> { n.setRead(true); repository.save(n); });
    }
}


