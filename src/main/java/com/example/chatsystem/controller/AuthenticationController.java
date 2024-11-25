package com.example.chatsystem.controller;

import com.example.chatsystem.dto.auth.*;
import com.example.chatsystem.security.AuthService;
import com.example.chatsystem.security.MyUserDetails;
import com.example.chatsystem.service.UserService;
import com.example.chatsystem.utils.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v4/auth")
public class AuthenticationController {
    private final AuthService authService;
    private final UserService userService;

    @Autowired
    public AuthenticationController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response){
        JwtResponse jwtResponse = authService.authenticate(loginRequest);

        CookieUtils.addCookie(response, "jwt", jwtResponse.getAccessToken(),
                3600 * 24 * 7, request.getServerName());

        AuthResponse loginResponse = AuthResponse.builder()
                .username(jwtResponse.getUsername())
                .userId(jwtResponse.getId())
                .avatar(jwtResponse.getAvatar())
                .build();

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response){

        CookieUtils.addCookie(response, "jwt", "",
                0, request.getServerName());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate(@AuthenticationPrincipal MyUserDetails userDetails) {
        AuthResponse authResponse = AuthResponse.builder()
                .username(userDetails.getUsername())
                .userId(userDetails.getUserId())
                .avatar(userDetails.getAvatar())
                .build();
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest signupDTO) {
        SignupResponse response = userService.create(signupDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/demo")
    public ResponseEntity<AuthResponse> demo(HttpServletResponse response, HttpServletRequest httpServletRequest){
        JwtResponse jwtResponse = authService.demo();

        CookieUtils.addCookie(response, "jwt", jwtResponse.getAccessToken(),
                3600 * 24 * 7, httpServletRequest.getServerName());

        AuthResponse loginResponse = AuthResponse.builder()
                .username(jwtResponse.getUsername())
                .avatar(jwtResponse.getAvatar())
                .userId(jwtResponse.getId())
                .build();

        return ResponseEntity.ok(loginResponse);
    }
}
