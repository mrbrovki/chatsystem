package com.example.chatsystem.controller;

import com.amazonaws.services.s3.model.PutObjectResult;
import com.example.chatsystem.dto.EditUserDTO;
import com.example.chatsystem.dto.chat.PrivateChatResponseDTO;
import com.example.chatsystem.model.User;
import com.example.chatsystem.security.MyUserDetails;
import com.example.chatsystem.service.S3Service;
import com.example.chatsystem.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v2/users")
public class UserController {
    private final UserService userService;
    private final S3Service s3Service;


    @Autowired
    public UserController(UserService userService, S3Service s3Service) {
        this.userService = userService;
        this.s3Service = s3Service;
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

    @GetMapping
    public ResponseEntity<List<PrivateChatResponseDTO>> findAll(){
        return ResponseEntity.ok(userService.findAll());
    }

    @PostMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EditUserDTO> updateUser(@AuthenticationPrincipal MyUserDetails userDetails, @RequestPart("json") String json, @RequestPart("avatar") MultipartFile avatar) {

        EditUserDTO editRequest;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            editRequest = objectMapper.readValue(json,  EditUserDTO.class);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        EditUserDTO editUserDTO = userService.edit(new ObjectId(userDetails.getUserId()), editRequest);

        try {
            PutObjectResult result = s3Service.uploadAvatar(avatar.getInputStream(), editUserDTO.getUsername(), avatar.getContentType());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok(editUserDTO);
    }
}