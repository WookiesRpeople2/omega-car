package com.example.view.auth;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.dto.AuthResponseDto;
import com.example.dto.SignupRequestDto;

@PageTitle("Sign up")
@Route("signup")
@AnonymousAllowed
public class SignupView extends VerticalLayout {

  private final WebClient webClient;
  private final String baseUrl;

  public SignupView(WebClient webClient, @Value("${app.base-url}") String baseUrl) {
    this.webClient = webClient;
    this.baseUrl = baseUrl;

    setSizeFull();
    setAlignItems(Alignment.CENTER);
    setJustifyContentMode(JustifyContentMode.CENTER);

    H1 title = new H1("Create account");

    TextField first = new TextField("First name");
    first.setRequired(true);

    TextField last = new TextField("Last name");
    last.setRequired(true);

    EmailField email = new EmailField("Email");
    email.setRequired(true);
    email.setErrorMessage("Please enter a valid email");

    PasswordField password = new PasswordField("Password");
    password.setRequired(true);
    password.setMinLength(8);
    password.setErrorMessage("Password must be at least 8 characters");

    Select<String> roleSelect = new Select<>();
    roleSelect.setLabel("Role");
    roleSelect.setItems("User", "Driver");
    roleSelect.setValue("User");
    roleSelect.setRequiredIndicatorVisible(true);
    roleSelect.setHelperText("Select your role: User (book rides) or Driver (provide rides)");

    Button signup = new Button("Sign up", e -> handleSignup(
        first.getValue(), 
        last.getValue(), 
        email.getValue(), 
        password.getValue(),
        roleSelect.getValue()
    ));
    signup.getStyle().set("margin-top", "10px");

    RouterLink loginLink = new RouterLink("Already have an account? Log in", LoginView.class);
    loginLink.getStyle().set("margin-top", "20px");

    FormLayout form = new FormLayout(first, last, email, password, roleSelect, signup);
    add(title, form, loginLink);
  }

  private void handleSignup(String firstName, String lastName, String email, String password, String role) {
    if (firstName == null || firstName.isEmpty() || 
        lastName == null || lastName.isEmpty() || 
        email == null || email.isEmpty() || 
        password == null || password.isEmpty() ||
        role == null || role.isEmpty()) {
      Notification.show("Please fill in all fields", 3000, Notification.Position.MIDDLE);
      return;
    }

    if (password.length() < 8) {
      Notification.show("Password must be at least 8 characters", 3000, Notification.Position.MIDDLE);
      return;
    }

    SignupRequestDto request = new SignupRequestDto();
    request.setFirstName(firstName);
    request.setLastName(lastName);
    request.setEmail(email);
    request.setPassword(password);
    request.setRole(role);

    try {
      AuthResponseDto response = webClient.post()
          .uri(baseUrl + "/api/auth/signup")
          .bodyValue(request)
          .retrieve()
          .bodyToMono(AuthResponseDto.class)
          .block();

      if (response != null) {
        Notification.show(
            response.getMessage() != null ? response.getMessage() : "Account created. Check your email for verification.",
            5000,
            Notification.Position.MIDDLE
        );
        UI.getCurrent().navigate("login");
      }
    } catch (Exception ex) {
      String raw = String.valueOf(ex.getMessage());
      String errorMessage = "Failed to create account. Please try again.";
      if (raw.contains("duplicate") || raw.contains("constraint") || raw.contains("Duplicate entry") || raw.contains("unique")) {
        errorMessage = "An account with this email already exists.";
      }
      Notification.show(errorMessage, 3000, Notification.Position.MIDDLE);
    }
  }
}


