package com.example.View.Auth;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.Dto.AuthResponseDto;
import com.example.Dto.LoginRequestDto;

import jakarta.servlet.http.Cookie;

import java.util.List;

@PageTitle("Login")
@Route("login")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

  private final WebClient webClient;
  private final String baseUrl;

  public LoginView(WebClient webClient, @Value("${app.base-url}") String baseUrl) {
    this.webClient = webClient;
    this.baseUrl = baseUrl;

    setSizeFull();
    setAlignItems(Alignment.CENTER);
    setJustifyContentMode(JustifyContentMode.CENTER);

    H1 title = new H1("Login");
    EmailField email = new EmailField("Email");
    email.setRequired(true);
    email.setErrorMessage("Please enter a valid email");

    PasswordField password = new PasswordField("Password");
    password.setRequired(true);

    Button login = new Button("Login", e -> handleLogin(email.getValue(), password.getValue()));
    login.getStyle().set("margin-top", "10px");

    RouterLink signupLink = new RouterLink("Don't have an account? Sign up", SignupView.class);
    signupLink.getStyle().set("margin-top", "20px");

    FormLayout form = new FormLayout(email, password, login);
    add(title, form, signupLink);
  }

  private void handleLogin(String email, String password) {
    if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
      Notification.show("Please fill in all fields", 3000, Notification.Position.MIDDLE);
      return;
    }

    LoginRequestDto request = new LoginRequestDto();
    request.setEmail(email);
    request.setPassword(password);

    try {
      AuthResponseDto response = webClient.post()
          .uri(baseUrl + "/api/auth/login")
          .bodyValue(request)
          .retrieve()
          .bodyToMono(AuthResponseDto.class)
          .block();
      
      if (response != null && response.getToken() != null) {
        Cookie cookie = new Cookie("AUTH", response.getToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(86400);
        VaadinService.getCurrentResponse().addCookie(cookie);
        
        // Manually set SecurityContext for immediate navigation
        String role = response.getRole();
        // Normalize role: First letter uppercase, rest lowercase (e.g., "user" -> "User")
        String normalizedRole = (role != null && !role.isEmpty()) 
          ? role.substring(0, 1).toUpperCase() + (role.length() > 1 ? role.substring(1).toLowerCase() : "")
          : "User";
        Authentication auth = new UsernamePasswordAuthenticationToken(
          email, 
          null, 
          List.of(new SimpleGrantedAuthority("ROLE_" + normalizedRole))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        Notification.show("Login successful!", 3000, Notification.Position.MIDDLE);
        
        if ("Admin".equals(normalizedRole)) {
          UI.getCurrent().navigate("admin");
        } else if ("Driver".equals(normalizedRole)) {
          UI.getCurrent().navigate("driver");
        } else {
          UI.getCurrent().navigate("user");
        }
      } else {
        Notification.show("Invalid credentials or email not verified", 3000, Notification.Position.MIDDLE);
      }
    } catch (Exception ex) {
      Notification.show("Login failed: Invalid credentials or email not verified", 3000, Notification.Position.MIDDLE);
    }
  }
}


