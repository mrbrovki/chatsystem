package com.example.chatsystem.security;

import com.example.chatsystem.dto.auth.AuthRequest;
import com.example.chatsystem.dto.auth.JwtResponse;
import com.example.chatsystem.service.DemoUserService;
import org.bson.types.ObjectId;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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

    public JwtResponse demo(){
        ObjectId demoUserId = demoUserService.findAvailableUserId();
        MyUserDetails userDetails = userDetailsService.loadUserByUserId(demoUserId);
        return jwtService.generateToken(userDetails);
    }
}