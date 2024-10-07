package com.example.chatsystem.controller;

import com.example.chatsystem.dto.auth.JwtResponse;
import com.example.chatsystem.dto.chat.PrivateChatResponse;
import com.example.chatsystem.dto.user.EditRequest;
import com.example.chatsystem.security.MyUserDetails;
import com.example.chatsystem.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v3/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/exists")
    public ResponseEntity<String> doesUserExist(@RequestParam String username) {
        boolean exists = userService.doesUsernameExist(username);

        if (!exists) {
            return ResponseEntity.ok("User with name '" + username + "' does not exist.");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("User with name '" + username + "' already exists.");
        }
    }

    @GetMapping
    public ResponseEntity<List<PrivateChatResponse>> findAll(){
        return ResponseEntity.ok(userService.findAll());
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JwtResponse> updateUser(@AuthenticationPrincipal MyUserDetails userDetails,
                                                  @RequestPart("json") String json,
                                                  @RequestPart("avatar") MultipartFile avatar) {

        EditRequest request;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            request = objectMapper.readValue(json, EditRequest.class);
            request.setAvatar(avatar);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JwtResponse response = userService.edit(new ObjectId(userDetails.getUserId()), request);
        return ResponseEntity.ok(response);
    }
}