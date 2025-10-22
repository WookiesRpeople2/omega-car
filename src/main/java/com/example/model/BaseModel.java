package com.example.model;

import java.sql.Timestamp;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@MappedSuperclass
public class BaseModel{
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "created_at")
  private Timestamp createdAt;
  @Column(name = "updated_at")
  private Timestamp updatedAt;

  @PrePersist
  protected void onCreate() {
      Timestamp now = new Timestamp(System.currentTimeMillis());
      this.createdAt = now;
      this.updatedAt = now;
  }

  @PreUpdate
  protected void onUpdate() {
      this.updatedAt = new Timestamp(System.currentTimeMillis());
  }

  public UUID getId(){
    return id;
  }
}
