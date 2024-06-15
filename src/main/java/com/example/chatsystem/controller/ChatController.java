package com.example.chatsystem.controller;

import com.example.chatsystem.dto.ChatResponseDTO;
import com.example.chatsystem.dto.GroupChatCreateDTO;
import com.example.chatsystem.model.GroupChat;
import com.example.chatsystem.security.MyUserDetails;
import com.example.chatsystem.service.ChatService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/v3/chats")
public class ChatController {
    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public ResponseEntity<List<ChatResponseDTO>> findAllChats(@AuthenticationPrincipal MyUserDetails userDetails) {
        ArrayList<ChatResponseDTO> chatsDTOs = chatService.findAllChats(new ObjectId(userDetails.getUserId()));
        return ResponseEntity.ok(chatsDTOs);
    }

    @PostMapping
    public ResponseEntity<GroupChat> createGroupChat(@AuthenticationPrincipal MyUserDetails userDetails, @RequestBody GroupChatCreateDTO groupChatCreateDTO) {
        GroupChat createdGroupChat = chatService.createGroupChat(new ObjectId(userDetails.getUserId()), groupChatCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGroupChat);
    }

    @GetMapping("/private")
    public ResponseEntity<List<ChatResponseDTO>> findPrivateChats(@AuthenticationPrincipal MyUserDetails userDetails) {
        ArrayList<ChatResponseDTO> chatsDTOs = chatService.findPrivateChats(new ObjectId(userDetails.getUserId()));
        return ResponseEntity.ok(chatsDTOs);
    }

    @GetMapping("/public")
    public ResponseEntity<List<ChatResponseDTO>> findGroupChats(@AuthenticationPrincipal MyUserDetails userDetails) {
        ArrayList<ChatResponseDTO> chatsDTOs = chatService.findGroupChats(new ObjectId(userDetails.getUserId()));
        return ResponseEntity.ok(chatsDTOs);
    }

    @PutMapping("/{id}/host")
    public ResponseEntity<GroupChat> changeGroupChatHost(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable("id") String chatId, @RequestParam String newHostName) {
        GroupChat updatedGroupChat = chatService.changeGroupChatHost(new ObjectId(userDetails.getUserId()), new ObjectId(chatId), newHostName);
        return ResponseEntity.ok(updatedGroupChat);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroupChat(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable("id") String chatId) {
        chatService.deleteGroupChat(new ObjectId(userDetails.getUserId()), new ObjectId(chatId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<String>> findGroupChatMemberNames(@PathVariable("id") String chatId) {
        ArrayList<String> chatsDTOs = chatService.findGroupChatMemberNames(new ObjectId(chatId));
        return ResponseEntity.ok(chatsDTOs);
    }

    @PutMapping("/{id}/members/add")
    public ResponseEntity<GroupChat> addMemberToGroupChat(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable("id") String chatId, @RequestParam String memberName) {
        GroupChat updatedGroupChat = chatService.addMemberToGroupChat(new ObjectId(userDetails.getUserId()),
                new ObjectId(chatId), memberName);
        return ResponseEntity.ok(updatedGroupChat);
    }

    @PutMapping("/{id}/members/remove")
    public ResponseEntity<GroupChat> removeMemberFromGroupChat(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable("id") String chatId, @RequestParam String memberName) {
        GroupChat updatedGroupChat = chatService.removeMemberFromGroupChat(new ObjectId(userDetails.getUserId()),
                new ObjectId(chatId), memberName);
        return ResponseEntity.ok(updatedGroupChat);
    }

    @PutMapping("/{id}/name/update")
    public ResponseEntity<GroupChat> changeGroupChatName(@AuthenticationPrincipal MyUserDetails userDetails,
                                                    @PathVariable("id") String chatId,
                                                    @RequestParam String newName) {
        GroupChat updatedGroupChat = chatService.changeGroupChatName(new ObjectId(userDetails.getUserId()),
                new ObjectId(chatId), newName);
        return ResponseEntity.ok(updatedGroupChat);
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<Void> joinGroupChat(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable("id") String chatId) {
        chatService.addUserToGroup(new ObjectId(userDetails.getUserId()), new ObjectId(chatId));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/leave")
    public ResponseEntity<Void> leaveGroupChat(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable("id") String chatId) {
        chatService.removeUserFromGroup(new ObjectId(userDetails.getUserId()), new ObjectId(chatId));
        return ResponseEntity.noContent().build();
    }
}
