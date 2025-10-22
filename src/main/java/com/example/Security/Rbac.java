package com.example.Security;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class Rbac {
  public static boolean hasAnyRole(String... roles) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) return false;
    Set<String> need = new HashSet<>();
    Arrays.stream(roles).forEach(r -> need.add("ROLE_" + normalize(r)));
    for (GrantedAuthority ga : auth.getAuthorities()) {
      if (need.contains(ga.getAuthority())) return true;
    }
    return false;
  }

  private static String normalize(String r) {
    if (r == null) return "";
    String s = r.trim();
    if (s.isEmpty()) return s;
    return Character.toUpperCase(s.charAt(0)) + (s.length() > 1 ? s.substring(1).toLowerCase() : "");
  }
}


