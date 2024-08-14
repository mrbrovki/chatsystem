package com.example.chatsystem.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.chatsystem.dto.EditUserDTO;
import com.example.chatsystem.model.User;
import com.example.chatsystem.security.MyUserDetails;
import com.example.chatsystem.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v2/users")
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final AmazonS3 amazonS3;

    @Autowired
    public UserController(UserService userService, PasswordEncoder passwordEncoder, ObjectMapper objectMapper, AmazonS3 amazonS3) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
        this.amazonS3 = amazonS3;
    }

    @GetMapping
    public ResponseEntity<String> getUserName(@AuthenticationPrincipal UserDetails user){
        return ResponseEntity.ok(user.getUsername());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") ObjectId id){
        User user = userService.findById(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/username")
    public ResponseEntity<User> getUserByUsername(@RequestParam("username") String username){
        User user = userService.findByUsername(username);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    //  for testing only
    @GetMapping("/all")
    public ResponseEntity<List<User>> findAll(){
        List<User> user = userService.findAll();
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> updateUser(@AuthenticationPrincipal MyUserDetails userDetails, @RequestPart("json") String json, @RequestPart("avatar") MultipartFile avatar){
        User user = userService.findById(new ObjectId(userDetails.getUserId()));
        System.out.println(avatar.getName());

        EditUserDTO editUserDTO = null;
        try {
            editUserDTO = objectMapper.readValue(json,  EditUserDTO.class);
            PutObjectRequest putObjectRequest = new PutObjectRequest("chatbucket69", "avatar_" + userDetails.getUserId(),
                    avatar.getInputStream(), new ObjectMetadata());
            amazonS3.putObject(putObjectRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        user.setUsername(editUserDTO.getUsername());
        user.setHashedPassword(passwordEncoder.encode(editUserDTO.getPassword()));
        userService.create(user);
        return ResponseEntity.ok(user);
    }
}