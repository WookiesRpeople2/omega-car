package com.example.Security;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtService {
  private final SecretKey signingKey;
  private final SecretKey aesKey;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  public JwtService(
      @Value("${jwt.signing.secret}") String signingSecret,
      @Value("${jwt.aes.key}") String aesKeyBase64
  ) {
    this.signingKey = Keys.hmacShaKeyFor(signingSecret.getBytes(StandardCharsets.UTF_8));
    byte[] aesKeyBytes = Base64.getDecoder().decode(aesKeyBase64);
    this.aesKey = new SecretKeySpec(aesKeyBytes, "AES");
  }

  public String createEncryptedJwt(String subject, Map<String, Object> claims, long ttlSeconds) {
    Instant now = Instant.now();
    String jwt = Jwts.builder()
        .subject(subject)
        .claims(claims)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(ttlSeconds)))
        .signWith(signingKey)
        .compact();
    return encrypt(jwt);
  }

  public Map<String, Object> parseEncryptedJwt(String token) {
    String jwt = decrypt(token);
    return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(jwt).getPayload();
  }

  private String encrypt(String plaintext) {
    try {
      byte[] iv = new byte[12];
      SECURE_RANDOM.nextBytes(iv);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      GCMParameterSpec spec = new GCMParameterSpec(128, iv);
      cipher.init(Cipher.ENCRYPT_MODE, aesKey, spec);
      byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
      ByteBuffer bb = ByteBuffer.allocate(4 + iv.length + ciphertext.length);
      bb.putInt(iv.length);
      bb.put(iv);
      bb.put(ciphertext);
      return Base64.getUrlEncoder().withoutPadding().encodeToString(bb.array());
    } catch (Exception e) {
      throw new IllegalStateException("JWT encryption failed", e);
    }
  }

  private String decrypt(String token) {
    try {
      byte[] data = Base64.getUrlDecoder().decode(token);
      ByteBuffer bb = ByteBuffer.wrap(data);
      int ivLen = bb.getInt();
      byte[] iv = new byte[ivLen];
      bb.get(iv);
      byte[] ciphertext = new byte[bb.remaining()];
      bb.get(ciphertext);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      GCMParameterSpec spec = new GCMParameterSpec(128, iv);
      cipher.init(Cipher.DECRYPT_MODE, aesKey, spec);
      byte[] plaintext = cipher.doFinal(ciphertext);
      return new String(plaintext, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new IllegalStateException("JWT decryption failed", e);
    }
  }
}


