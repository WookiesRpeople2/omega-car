package com.example.Repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.example.Model.User;

@Repository
public interface UserRepository extends BaseRepository<User, UUID> {
  Optional<User> findByEmail(String email);
}


