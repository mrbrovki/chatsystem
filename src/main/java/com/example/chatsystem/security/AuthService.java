package com.example.chatsystem.security;

import com.example.chatsystem.dto.auth.AuthRequest;
import com.example.chatsystem.dto.auth.JwtResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class  AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public JwtResponse authenticate(AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        if(authentication.isAuthenticated()){
            return jwtService.generateToken(userDetails);
        } else {
            throw new UsernameNotFoundException("invalid user request..!!");
        }
    }
}