package com.example.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.example.model.User;

@Repository
public interface UserRepository extends BaseRepository<User, UUID> {
  Optional<User> findByEmail(String email);
}


