package com.example.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.Notification;
import com.example.repository.UserRepository;
import com.example.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("hasAnyRole('Driver','Admin')")
public class NotificationController {

    private final NotificationService notifications;
    private final UserRepository users;

    public NotificationController(NotificationService notifications, UserRepository users) {
        this.notifications = notifications;
        this.users = users;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> myNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return ResponseEntity.status(401).build();
        return users.findByEmail(auth.getName())
            .map(u -> ResponseEntity.ok(notifications.listForUser(u.getId())))
            .orElse(ResponseEntity.status(401).build());
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable("id") UUID id) {
        notifications.markRead(id);
        return ResponseEntity.noContent().build();
    }
}


