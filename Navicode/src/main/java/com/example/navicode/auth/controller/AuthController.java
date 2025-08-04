package com.example.navicode.auth.controller;

import com.example.navicode.auth.dto.LoginRequest;
import com.example.navicode.auth.dto.LoginResponse;
import com.example.navicode.auth.dto.RegisterRequest;
import com.example.navicode.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest.getUsername(), loginRequest.getPassword());
    }

    @PostMapping("/register")
    public LoginResponse register(@RequestBody RegisterRequest registerRequest) {
        return authService.register(registerRequest.getUsername(), registerRequest.getPassword());
    }
}