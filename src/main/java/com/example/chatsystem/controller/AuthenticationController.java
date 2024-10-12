package com.example.chatsystem.controller;

import com.example.chatsystem.dto.auth.*;
import com.example.chatsystem.security.AuthService;
import com.example.chatsystem.security.MyUserDetails;
import com.example.chatsystem.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v3/auth")
public class AuthenticationController {
    private final AuthService authService;
    private final UserService userService;

    @Autowired
    public AuthenticationController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest, HttpServletResponse response){
        JwtResponse jwtResponse = authService.authenticate(authRequest);

        Cookie cookie = new Cookie("jwt", jwtResponse.getAccessToken());
        cookie.setPath("/");
        cookie.setMaxAge(3600 * 24 * 7);
        cookie.setDomain("localhost");
        cookie.setSecure(false);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        AuthResponse loginResponse = AuthResponse.builder()
                .username(jwtResponse.getUsername())
                .avatar(jwtResponse.getAvatar())
                .build();

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response){

        Cookie cookie = new Cookie("jwt", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setDomain("localhost");
        cookie.setSecure(false);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate(@AuthenticationPrincipal MyUserDetails userDetails) {
        AuthResponse authResponse = AuthResponse.builder()
                .username(userDetails.getUsername())
                .avatar(userDetails.getAvatar())
                .build();
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest signupDTO){
        SignupResponse response = userService.create(signupDTO);
        return ResponseEntity.ok(response);
    }
}
