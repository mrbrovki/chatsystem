package com.example.chatsystem.controller;

import com.example.chatsystem.dto.auth.JwtResponse;
import com.example.chatsystem.dto.chat.PrivateChatResponse;
import com.example.chatsystem.dto.user.EditRequest;
import com.example.chatsystem.dto.user.EditResponse;
import com.example.chatsystem.security.MyUserDetails;
import com.example.chatsystem.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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

    @PutMapping(value = "/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EditResponse> editUser(@AuthenticationPrincipal MyUserDetails userDetails,
                                                 @RequestPart("json") String json,
                                                 @RequestPart("avatar") MultipartFile avatar,
                                                 HttpServletResponse response) {

        EditRequest request;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            request = objectMapper.readValue(json, EditRequest.class);
            request.setAvatar(avatar);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JwtResponse jwtResponse = userService.edit(new ObjectId(userDetails.getUserId()), request);

        Cookie cookie = new Cookie("jwt", jwtResponse.getAccessToken());
        cookie.setPath("/");
        cookie.setMaxAge(3600 * 24 * 7);
        cookie.setDomain("localhost");
        cookie.setSecure(false);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        EditResponse editResponse = EditResponse.builder()
                .avatar(jwtResponse.getAvatar())
                .username(jwtResponse.getUsername())
                .build();

        return ResponseEntity.ok(editResponse);
    }
}