package com.example.chatsystem.controller;

import com.example.chatsystem.dto.auth.LoginRequest;
import com.example.chatsystem.dto.auth.SignupRequest;
import com.example.chatsystem.dto.auth.JwtResponse;
import com.example.chatsystem.dto.auth.SignupResponse;
import com.example.chatsystem.security.AuthService;
import com.example.chatsystem.security.MyUserDetails;
import com.example.chatsystem.service.UserService;
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
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest loginRequest){
        JwtResponse responseDTO = authService.login(loginRequest);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<JwtResponse> authenticate(@AuthenticationPrincipal MyUserDetails userDetails) {
        return ResponseEntity.ok(authService.authenticate(userDetails));
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest signupDTO){
        SignupResponse response = userService.create(signupDTO);
        return ResponseEntity.ok(response);
    }
}
