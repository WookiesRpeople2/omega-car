package com.example.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.model.VerificationToken;

@Repository
public interface VerificationTokenRepository extends BaseRepository<VerificationToken, java.util.UUID> {
  Optional<VerificationToken> findByToken(String token);
}


