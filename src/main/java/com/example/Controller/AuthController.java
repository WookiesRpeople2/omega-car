package com.example.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Dto.AuthResponseDto;
import com.example.Dto.LoginRequestDto;
import com.example.Dto.SignupRequestDto;
import com.example.Model.User;
import com.example.Service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponseDto> signup(@Valid @RequestBody SignupRequestDto request) {
        try {
            User user = userService.signup(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPassword(),
                request.getRole()
            );
            
            AuthResponseDto response = new AuthResponseDto(
                null,
                "Account created successfully. Please check your email to verify your account."
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            AuthResponseDto response = new AuthResponseDto(null, "Failed to create account: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        return userService.loginAndIssueToken(request.getEmail(), request.getPassword())
            .map(result -> {
                AuthResponseDto response = new AuthResponseDto(
                    result.get("token"), 
                    "Login successful", 
                    result.get("role")
                );
                return ResponseEntity.ok(response);
            })
            .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthResponseDto(null, "Invalid credentials or email not verified", null)));
    }
}

