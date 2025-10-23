package com.example.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.model.User;
import com.example.model.VerificationToken;
import com.example.repository.UserRepository;
import com.example.repository.VerificationTokenRepository;
import com.example.security.JwtService;
import com.example.security.PasswordHasher;

@Service
public class UserService {
  private final UserRepository userRepository;
  private final VerificationTokenRepository verificationTokenRepository;
  private final JwtService jwtService;
  private final MailService mailService;

  public UserService(UserRepository userRepository, VerificationTokenRepository verificationTokenRepository,
      JwtService jwtService, MailService mailService) {
    this.userRepository = userRepository;
    this.verificationTokenRepository = verificationTokenRepository;
    this.jwtService = jwtService;
    this.mailService = mailService;
  }

  @Transactional
  public User signup(String firstName, String lastName, String email, String plainPassword, String role) {
    if (role == null || (!"User".equals(role) && !"Driver".equals(role))) {
      role = "User";
    }

    if (userRepository.findByEmail(email).isPresent()) {
      throw new IllegalArgumentException("duplicate email");
    }

    User user = new User();
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setEmail(email);
    user.setRole(role);
    user.setEmailValidated(false);
    String salt = PasswordHasher.generateSaltBase64();
    user.setPasswordSalt(salt);
    user.setPassword(PasswordHasher.hashPassword(plainPassword.toCharArray(), salt));
    user = userRepository.save(user);
    String token = createAndSaveVerificationToken(user.getId());
    mailService.sendVerificationEmail(user.getEmail(), token);
    return user;
  }

  @Transactional
  public Optional<Map<String, String>> loginAndIssueToken(String email, String password) {
    return userRepository.findByEmail(email)
        .filter(u -> Boolean.TRUE.equals(u.getEmailValidated()))
        .filter(u -> PasswordHasher.verifyPassword(password.toCharArray(), u.getPasswordSalt(), u.getPassword()))
        .map(u -> {
          String token = jwtService.createEncryptedJwt(u.getId().toString(),
              Map.of("role", u.getRole(), "email", u.getEmail()), 86400);
          return Map.of("token", token, "role", u.getRole());
        });
  }

  @Transactional
  public void changeRole(UUID userId, String role) {
    userRepository.findById(userId).ifPresent(u -> {
      u.setRole(role);
      userRepository.save(u);
    });
  }

  @Transactional
  public boolean verifyEmail(String token) {
    Optional<VerificationToken> vt = verificationTokenRepository.findByToken(token);
    if (vt.isEmpty()) {
      return false;
    }
    VerificationToken v = vt.get();
    if (v.isConsumed()) {
      return false;
    }
    if (v.getExpiresAt().toInstant().isBefore(Instant.now())) {
      return false;
    }
    userRepository.findById(v.getUserId()).ifPresent(u -> {
      u.setEmailValidated(true);
      userRepository.save(u);
    });
    v.setConsumed(true);
    verificationTokenRepository.save(v);
    return true;
  }

  @Transactional
  public Optional<User> getUserById(UUID userId) {
    return userRepository.findById(userId);
  }

  @Transactional
  public Optional<User> getUserByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  private String createAndSaveVerificationToken(UUID userId) {
    VerificationToken v = new VerificationToken();
    v.setToken(UUID.randomUUID().toString());
    v.setUserId(userId);
    v.setExpiresAt(Timestamp.from(Instant.now().plusSeconds(48 * 3600)));
    v.setConsumed(false);
    verificationTokenRepository.save(v);
    return v.getToken();
  }
}
