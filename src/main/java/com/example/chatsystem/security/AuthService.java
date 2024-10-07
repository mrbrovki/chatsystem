package com.example.chatsystem.security;

import com.example.chatsystem.dto.auth.JwtResponse;
import com.example.chatsystem.dto.auth.LoginRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public JwtResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        if(authentication.isAuthenticated()){
            return JwtResponse.builder()
                    .accessToken(jwtService.GenerateToken(loginRequest.getUsername(), userDetails.getUserId()))
                    .username(userDetails.getUsername())
                    .avatar(userDetails.getAvatar())
                    .build();
        } else {
            throw new UsernameNotFoundException("invalid user request..!!");
        }
    }

    public JwtResponse authenticate(MyUserDetails userDetails) {
        return JwtResponse.builder()
                .accessToken(jwtService.GenerateToken(userDetails.getUsername(), userDetails.getUserId()))
                .username(userDetails.getUsername())
                .avatar(userDetails.getAvatar())
                .build();
    }
}