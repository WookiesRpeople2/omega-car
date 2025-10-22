package com.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {
  private final JavaMailSender mailSender;
  private final String baseUrl;

  public MailService(JavaMailSender mailSender, @Value("${app.base-url}") String baseUrl) {
    this.mailSender = mailSender;
    this.baseUrl = baseUrl;
  }

  public void sendVerificationEmail(String to, String token) {
    String verifyLink = baseUrl + "/verify-email?token=" + token;
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setTo(to);
    msg.setSubject("Verify your email");
    msg.setText("Please verify your email by clicking: " + verifyLink);
    mailSender.send(msg);
  }
}


