package com.example.chatsystem.controller;

import com.example.chatsystem.model.User;
import com.example.chatsystem.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
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

    @PutMapping("/{userId}/chats/{chatId}/add")
    public ResponseEntity<List<String>> addChatToUserById(@PathVariable("userId") ObjectId userId, @PathVariable("chatId") String chatId) {
        List<String> updatedChatIds = userService.addPrivateChatToUser(userId, chatId);
        return ResponseEntity.ok(updatedChatIds);
    }

    @PutMapping("/{userId}/chats/{chatId}/remove")
    public ResponseEntity<List<String>> removeChatFromUserById(@PathVariable("userId") ObjectId userId, @PathVariable("chatId") String chatId) {
        List<String> updatedChatIds = userService.removePrivateChatFromUser(userId, chatId);
        return ResponseEntity.ok(updatedChatIds);
    }

    @PutMapping("/{userId}/groups/{chatId}/add")
    public ResponseEntity<List<ObjectId>> addChatToUserById(@PathVariable("userId") ObjectId userId, @PathVariable("chatId") ObjectId chatId) {
        List<ObjectId> updatedChatIds = userService.addGroupChatToUser(userId, chatId);
        return ResponseEntity.ok(updatedChatIds);
    }

    @PutMapping("/{userId}/groups/{chatId}/remove")
    public ResponseEntity<List<ObjectId>> removeChatFromUserById(@PathVariable("userId") ObjectId userId, @PathVariable("chatId") ObjectId chatId) {
        List<ObjectId> updatedChatIds = userService.removeGroupChatFromUser(userId, chatId);
        return ResponseEntity.ok(updatedChatIds);
    }
}
