package com.example.chatsystem.controller;

import com.example.chatsystem.dto.AuthRequestDTO;
import com.example.chatsystem.dto.AuthSignupDTO;
import com.example.chatsystem.dto.JwtResponseDTO;
import com.example.chatsystem.model.User;
import com.example.chatsystem.security.JwtService;
import com.example.chatsystem.security.MyUserDetails;
import com.example.chatsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("api/v3/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationController(AuthenticationManager authenticationManager, JwtService jwtService, UserService userService, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public JwtResponseDTO login(@RequestBody AuthRequestDTO authRequestDTO){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequestDTO.getUsername(), authRequestDTO.getPassword()));
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        if(authentication.isAuthenticated()){
            return JwtResponseDTO.builder()
                    .accessToken(jwtService.GenerateToken(authRequestDTO.getUsername(), userDetails.getUserId())).build();
        } else {
            throw new UsernameNotFoundException("invalid user request..!!");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody AuthSignupDTO signupDTO){
        User newUser = User.builder()
                .email(signupDTO.getEmail())
                .username(signupDTO.getUsername())
                .hashedPassword(passwordEncoder.encode(signupDTO.getPassword()))
                .groupChats(new ArrayList<>())
                .chats(new ArrayList<>())
                .build();
        userService.create(newUser);
        return ResponseEntity.ok(newUser.getEmail());
    }
}
