package com.example.security;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordHasher {
  private static final int SALT_BYTES = 16;
  private static final int ITERATIONS = 210000;
  private static final int KEY_LENGTH = 256;

  private PasswordHasher() {
    throw new UnsupportedOperationException("This class is not instantiable");
  }

  public static String generateSaltBase64() {
    byte[] salt = new byte[SALT_BYTES];
    new SecureRandom().nextBytes(salt);
    return Base64.getEncoder().encodeToString(salt);
  }

  public static String hashPassword(char[] password, String saltBase64) {
    byte[] salt = Base64.getDecoder().decode(saltBase64);
    PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
    try {
      SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
      byte[] hash = skf.generateSecret(spec).getEncoded();
      return Base64.getEncoder().encodeToString(hash);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new IllegalStateException("Password hashing failed", e);
    } finally {
      spec.clearPassword();
    }
  }

  public static boolean verifyPassword(char[] password, String saltBase64, String expectedHashBase64) {
    String actualHash = hashPassword(password, saltBase64);
    return constantTimeEquals(expectedHashBase64, actualHash);
  }

  private static boolean constantTimeEquals(String a, String b) {
    byte[] ba = a.getBytes(StandardCharsets.UTF_8);
    byte[] bb = b.getBytes(StandardCharsets.UTF_8);
    if (ba.length != bb.length) {
      return false;
    }
    int result = 0;
    for (int i = 0; i < ba.length; i++) {
      result |= ba[i] ^ bb[i];
    }
    return result == 0;
  }
}
