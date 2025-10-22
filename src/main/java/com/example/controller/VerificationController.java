package com.example.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.service.UserService;

@RestController
@RequestMapping("/api/verify")
public class VerificationController {

  private final UserService userService;

  public VerificationController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/email")
  public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
    boolean ok = userService.verifyEmail(token);
    return ok ? ResponseEntity.noContent().build() : ResponseEntity.badRequest().build();
  }
}


