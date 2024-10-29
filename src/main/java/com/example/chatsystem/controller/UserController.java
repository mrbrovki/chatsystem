package com.example.chatsystem.controller;

import com.example.chatsystem.dto.auth.JwtResponse;
import com.example.chatsystem.dto.chat.PrivateChatResponse;
import com.example.chatsystem.dto.user.EditRequest;
import com.example.chatsystem.dto.user.EditResponse;
import com.example.chatsystem.security.MyUserDetails;
import com.example.chatsystem.service.UserService;
import com.example.chatsystem.utils.CookieUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v4/users")
public class UserController {
    private final UserService userService;
    private final Validator validator;

    @Autowired
    public UserController(UserService userService, Validator validator) {
        this.userService = userService;
        this.validator = validator;
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
                                                 HttpServletRequest httpServletRequest,
                                                 HttpServletResponse response) {

        EditRequest request;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            request = objectMapper.readValue(json, EditRequest.class);
            Errors errors = validator.validateObject(request);
            if (errors.hasErrors()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            request.setAvatar(avatar);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JwtResponse jwtResponse = userService.edit(new ObjectId(userDetails.getUserId()), request);

        CookieUtils.addCookie(response, "jwt", jwtResponse.getAccessToken(),
                3600 * 24 * 7, httpServletRequest.getServerName());

        EditResponse editResponse = EditResponse.builder()
                .avatar(jwtResponse.getAvatar())
                .username(jwtResponse.getUsername())
                .build();

        return ResponseEntity.ok(editResponse);
    }
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal MyUserDetails userDetails,
                                           HttpServletRequest request,HttpServletResponse response) {
        boolean isSuccess = userService.delete(new ObjectId(userDetails.getUserId()));
        CookieUtils.addCookie(response, "jwt", "",
                0, request.getServerName());
        return ResponseEntity.noContent().build();
    }
}