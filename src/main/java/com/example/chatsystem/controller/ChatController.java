package com.example.chatsystem.controller;

import com.example.chatsystem.model.GroupChat;
import com.example.chatsystem.service.ChatService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/chats")
public class ChatController {
    ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupChat> getChatById(@PathVariable("id") ObjectId id) {
        GroupChat groupChat = chatService.findById(id);
        if (groupChat != null) {
            return ResponseEntity.ok(groupChat);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<GroupChat> createChat(@RequestBody GroupChat groupChat) {
        GroupChat createdGroupChat = chatService.createChat(groupChat);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGroupChat);
    }

    @PutMapping("/{id}/host")
    public ResponseEntity<GroupChat> changeChatHost(@PathVariable("id") ObjectId id, @RequestParam String host) {
        GroupChat updatedGroupChat = chatService.changeChatHost(id, host);
        return ResponseEntity.ok(updatedGroupChat);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChat(@PathVariable("id") ObjectId id) {
        chatService.deleteChat(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/members/add")
    public ResponseEntity<GroupChat> addMemberToChat(@PathVariable("id") ObjectId chatId, @RequestParam ObjectId memberId) {
        GroupChat updatedGroupChat = chatService.addMemberToChat(chatId, memberId);
        return ResponseEntity.ok(updatedGroupChat);
    }

    @PutMapping("/{id}/members/remove")
    public ResponseEntity<GroupChat> removeMemberFromChat(@PathVariable("id") ObjectId chatId, @RequestParam ObjectId memberId) {
        GroupChat updatedGroupChat = chatService.removeMemberFromChat(chatId, memberId);
        return ResponseEntity.ok(updatedGroupChat);
    }

    @PutMapping("/{id}/name/update")
    public ResponseEntity<GroupChat> changeChatName(@PathVariable("id") ObjectId chatId, @RequestParam String newName) {
        GroupChat updatedGroupChat = chatService.changeChatName(chatId, newName);
        return ResponseEntity.ok(updatedGroupChat);
    }

    @GetMapping
    public ResponseEntity<List<GroupChat>> getAllChats() {
        List<GroupChat> groupChats = chatService.findAllChats();
        return ResponseEntity.ok(groupChats);
    }
}
