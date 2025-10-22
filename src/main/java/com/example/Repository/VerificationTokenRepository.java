package com.example.Repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.Model.VerificationToken;

@Repository
public interface VerificationTokenRepository extends BaseRepository<VerificationToken, java.util.UUID> {
  Optional<VerificationToken> findByToken(String token);
}


