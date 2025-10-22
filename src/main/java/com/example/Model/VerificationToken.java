package com.example.Model;

import java.sql.Timestamp;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(indexes = {
  @Index(name = "idx_verification_token", columnList = "token", unique = true)
})
public class VerificationToken extends BaseModel {
  @Column(nullable = false, unique = true)
  private String token;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "expires_at", nullable = false)
  private Timestamp expiresAt;

  @Column(name = "consumed", nullable = false)
  private boolean consumed;

  public String getToken() { return token; }
  public void setToken(String token) { this.token = token; }
  public UUID getUserId() { return userId; }
  public void setUserId(UUID userId) { this.userId = userId; }
  public Timestamp getExpiresAt() { return expiresAt; }
  public void setExpiresAt(Timestamp expiresAt) { this.expiresAt = expiresAt; }
  public boolean isConsumed() { return consumed; }
  public void setConsumed(boolean consumed) { this.consumed = consumed; }
}


