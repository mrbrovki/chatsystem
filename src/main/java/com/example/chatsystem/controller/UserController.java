package com.example.chatsystem.controller;

import com.example.chatsystem.dto.EditUserDTO;
import com.example.chatsystem.model.User;
import com.example.chatsystem.security.MyUserDetails;
import com.example.chatsystem.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/users")
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
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

    @PostMapping("/update")
    public ResponseEntity<User> updateUser(@AuthenticationPrincipal MyUserDetails userDetails, @RequestBody EditUserDTO editUserDTO){
        User user = userService.findById(new ObjectId(userDetails.getUserId()));
        user.setUsername(editUserDTO.getUsername());
        user.setHashedPassword(passwordEncoder.encode(editUserDTO.getPassword()));
        userService.create(user);
        return ResponseEntity.ok(user);
    }
}