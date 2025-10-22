package com.example.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Builder
@Entity
public class User extends BaseModel{
  @Size(min = 1, message = "{EMPTY_INPUT}")
  private String first_name;

  @Size(min = 1, message = "{EMPTY_INPUT}")
  private String last_name;

  @Pattern(regexp = "Admin|Driver|User", message = "{BAD_ROLE}")
  private String role;
  
  @Email
  private String email;

  @Column(name = "mobile_phone")
  @Pattern(regexp = "\\d{10}", message = "{BAD_TELEPHONE}")  
  private String mobilePhone;

  @Size(min = 1, message = "{EMPTY_INPUT}")
  private String password;
  
  @Column(name = "password_salt")
  private String passwordSalt;
  
  @Column(name = "email_validated")
  private Boolean emailValidated;
}
