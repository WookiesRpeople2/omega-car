package com.example.view.auth;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

@PageTitle("Verify Email")
@Route("verify-email")
@AnonymousAllowed
public class VerifyEmailView extends VerticalLayout implements BeforeEnterObserver {

  private final WebClient webClient;
  private final String baseUrl;
  private H2 title;
  private Paragraph message;

  public VerifyEmailView(WebClient webClient, @Value("${app.base-url}") String baseUrl) {
    this.webClient = webClient;
    this.baseUrl = baseUrl;

    setSizeFull();
    setAlignItems(Alignment.CENTER);
    setJustifyContentMode(JustifyContentMode.CENTER);

    // Initialize components that will be updated in beforeEnter
    title = new H2("Verifying...");
    title.getStyle().set("color", "#6b7280");

    message = new Paragraph("Please wait while we verify your email address...");
    message.getStyle().set("text-align", "center");
    message.getStyle().set("max-width", "400px");

    RouterLink loginLink = new RouterLink("Go to login", LoginView.class);
    loginLink.getStyle().set("margin-top", "20px");

    add(title, message, loginLink);
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    // Get token from query parameters
    String token = event.getLocation()
        .getQueryParameters()
        .getParameters()
        .getOrDefault("token", java.util.List.of())
        .stream()
        .findFirst()
        .orElse(null);

    boolean verified = verifyEmailToken(token);

    // Update the UI based on verification result
    title.setText(verified ? "✓ Email Verified" : "✗ Verification Failed");
    title.getStyle().set("color", verified ? "#10b981" : "#ef4444");

    message.setText(
        verified 
            ? "Your email has been successfully verified. You can now log in to your account." 
            : "Invalid or expired verification token. Please try again or request a new verification email."
    );
  }

  private boolean verifyEmailToken(String token) {
    if (token == null || token.isEmpty()) {
      return false;
    }

    try {
      webClient.get()
          .uri(baseUrl + "/api/verify/email?token=" + token)
          .retrieve()
          .toBodilessEntity()
          .block();
      return true;
    } catch (Exception ex) {
      return false;
    }
  }
}


