package com.example.navicode.auth.service;

import com.example.navicode.auth.dto.LoginResponse;
import com.example.navicode.auth.model.User;
import com.example.navicode.auth.repository.UserRepository;
import com.example.navicode.auth.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public LoginResponse login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            String token = jwtTokenProvider.createToken(username);
            return new LoginResponse(true, token, "로그인 성공");
        }
        return new LoginResponse(false, null, "아이디 또는 비밀번호가 올바르지 않습니다.");
    }

    public LoginResponse register(String username, String password) {
        if (userRepository.findByUsername(username) != null) {
            return new LoginResponse(false, null, "이미 존재하는 사용자입니다.");
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        userRepository.save(newUser);

        return new LoginResponse(true, null, "회원가입 성공");
    }
}