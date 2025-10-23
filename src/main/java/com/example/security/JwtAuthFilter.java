package com.example.security;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtService jwtService;

  public JwtAuthFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      String token = readBearerHeader(request);
      if (token == null) {
        token = readAuthCookie(request);
      }
      if (token != null) {
        try {
          Map<String, Object> claims = jwtService.parseEncryptedJwt(token);
          String email = (String) claims.get("email");
          String role = (String) claims.getOrDefault("role", "User");
          String normalizedRole = (role != null && !role.isEmpty())
              ? role.substring(0, 1).toUpperCase(Locale.ENGLISH)
                  + (role.length() > 1 ? role.substring(1).toLowerCase(Locale.ENGLISH) : "")
              : "User";
          String principal = email != null ? email : (String) claims.get("sub");
          Authentication auth = new UsernamePasswordAuthenticationToken(principal, null,
              java.util.List.of(new SimpleGrantedAuthority("ROLE_" + normalizedRole)));
          SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception ignored) {
        }
      }
    }
    filterChain.doFilter(request, response);
  }

  private String readAuthCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    for (Cookie c : cookies) {
      if ("AUTH".equals(c.getName())) {
        return c.getValue();
      }
    }
    return null;
  }

  private String readBearerHeader(HttpServletRequest request) {
    String auth = request.getHeader("Authorization");
    if (auth == null) return null;
    if (auth.startsWith("Bearer ")) {
      return auth.substring(7).trim();
    }
    return null;
  }
}
