package com.example.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


@Entity
public class User extends BaseModel{
  @Size(min = 1, message = "{EMPTY_INPUT}")
  private String firstName;

  @Size(min = 1, message = "{EMPTY_INPUT}")
  private String lastName;

  @Pattern(regexp = "Admin|Driver|User", message = "{BAD_ROLE}")
  private String role;
  
  @Email
  @Column(unique = true)
  private String email; 

  @Size(min = 1, message = "{EMPTY_INPUT}")
  @Column(nullable = false, length = 500)
  private String password;
  
  @Column(name = "password_salt", nullable = false, length = 100)
  private String passwordSalt;
  
  @Column(name = "email_validated", nullable = false, columnDefinition = "TINYINT(1)")
  private Boolean emailValidated;

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getPasswordSalt() {
    return passwordSalt;
  }

  public void setPasswordSalt(String passwordSalt) {
    this.passwordSalt = passwordSalt;
  }

  public Boolean getEmailValidated() {
    return emailValidated;
  }

  public void setEmailValidated(Boolean emailValidated) {
    this.emailValidated = emailValidated;
  }

  
}
