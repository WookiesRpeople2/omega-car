package com.example.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.service.UserService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
  private final UserService userService;

  public AdminController(UserService userService) {
    this.userService = userService;
  }

  @PatchMapping("/role")
  @PreAuthorize("hasRole('Admin')")
  public ResponseEntity<?> changeRole(@RequestBody Map<String, String> body) {
    UUID userId = UUID.fromString(body.get("userId"));
    String role = body.get("role");
    userService.changeRole(userId, role);
    return ResponseEntity.noContent().build();
  }
}


