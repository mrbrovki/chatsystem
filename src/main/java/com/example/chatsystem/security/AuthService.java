package com.example.chatsystem.security;

import com.example.chatsystem.dto.auth.LoginRequest;
import com.example.chatsystem.dto.auth.JwtResponse;
import com.example.chatsystem.service.DemoUserService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class  AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final DemoUserService demoUserService;
    private final MyUserDetailsService userDetailsService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService, DemoUserService demoUserService, MyUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.demoUserService = demoUserService;
        this.userDetailsService = userDetailsService;
    }

    public JwtResponse authenticate(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();

        if(authentication.isAuthenticated()){
            return JwtResponse.builder()
                    .accessToken(jwtService.generateToken(userDetails.getUserId()))
                    .id(userDetails.getUserId())
                    .username(userDetails.getUsername())
                    .avatar(userDetails.getAvatar())
                    .build();
        } else {
            throw new UsernameNotFoundException("invalid user request..!!");
        }
    }

    public JwtResponse demo(){
        UUID demoUserId = demoUserService.findAvailableUserId();
        MyUserDetails userDetails = userDetailsService.loadUserByUserId(demoUserId);
        return JwtResponse.builder()
                .accessToken(jwtService.generateToken(userDetails.getUserId()))
                .id(userDetails.getUserId())
                .username(userDetails.getUsername())
                .avatar(userDetails.getAvatar())
                .build();
    }
}