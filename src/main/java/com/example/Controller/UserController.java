package com.example.Controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Dto.UserDto;
import com.example.Model.User;
import com.example.Repository.UserRepository;

@RestController
@RequestMapping("/api/users")
public class UserController {
  private final UserRepository userRepository;

  public UserController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @GetMapping("/me")
  public ResponseEntity<UserDto> getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getName() == null) {
      return ResponseEntity.status(401).build();
    }
    
    String email = auth.getName();
    return userRepository.findByEmail(email)
      .map(this::toDto)
      .map(ResponseEntity::ok)
      .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  @PreAuthorize("hasRole('Admin')")
  public ResponseEntity<List<UserDto>> getAllUsers() {
    List<UserDto> users = userRepository.findAll().stream()
      .map(this::toDto)
      .collect(Collectors.toList());
    return ResponseEntity.ok(users);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('Admin')")
  public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
    return userRepository.findById(id)
      .map(this::toDto)
      .map(ResponseEntity::ok)
      .orElse(ResponseEntity.notFound().build());
  }

  private UserDto toDto(User user) {
    UserDto dto = new UserDto();
    dto.setId(user.getId());
    dto.setFirstName(user.getFirst_name());
    dto.setLastName(user.getLast_name());
    dto.setEmail(user.getEmail());
    dto.setRole(user.getRole());
    dto.setEmailValidated(user.getEmailValidated());
    dto.setMobilePhone(user.getMobilePhone());
    return dto;
  }
}


