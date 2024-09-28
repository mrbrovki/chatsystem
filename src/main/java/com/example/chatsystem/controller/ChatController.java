package com.example.chatsystem.controller;

import com.amazonaws.services.s3.model.PutObjectResult;
import com.example.chatsystem.dto.chat.*;
import com.example.chatsystem.model.GroupChat;
import com.example.chatsystem.security.MyUserDetails;
import com.example.chatsystem.service.ChatService;
import com.example.chatsystem.service.S3Service;
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

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/v3/chats")
public class ChatController {
    private final ChatService chatService;
    private final UserService userService;
    private final S3Service s3Service;

    @Autowired
    public ChatController(ChatService chatService, UserService userService, S3Service s3Service) {
        this.chatService = chatService;
        this.userService = userService;
        this.s3Service = s3Service;
    }

    @GetMapping
    public ResponseEntity<ChatResponseDTO> findAllChats(@AuthenticationPrincipal MyUserDetails userDetails) {
        ChatResponseDTO chatsDTOs = chatService.findAllChats(new ObjectId(userDetails.getUserId()));
        return ResponseEntity.ok(chatsDTOs);
    }

    @PostMapping(value = "/groups", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GroupChatResponseDTO> createGroupChat(@AuthenticationPrincipal MyUserDetails userDetails,
                                                                @RequestPart("json") String json,
                                                                @RequestPart("image") MultipartFile image) {
        GroupChatCreateDTO groupChatCreateDTO;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            groupChatCreateDTO = objectMapper.readValue(json,  GroupChatCreateDTO.class);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        GroupChatResponseDTO createdGroupChat = chatService.createGroupChat(new ObjectId(userDetails.getUserId()), groupChatCreateDTO);
        try {
            PutObjectResult result = s3Service.uploadAvatar(image.getInputStream(), createdGroupChat.getId(), image.getContentType());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(createdGroupChat);
    }

    @GetMapping("/private")
    public ResponseEntity<List<PrivateChatResponseDTO>> findPrivateChats(@AuthenticationPrincipal MyUserDetails userDetails) {
        ArrayList<PrivateChatResponseDTO> chatsDTOs = chatService.findPrivateChats(new ObjectId(userDetails.getUserId()));
        return ResponseEntity.ok(chatsDTOs);
    }

    @GetMapping("/private/{username}")
    public ResponseEntity<PrivateChatResponseDTO> findPrivateChatByName(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable String username) {
        return ResponseEntity.ok(chatService.findPrivateChatByName(new ObjectId(userDetails.getUserId()), username));
    }

    @PutMapping("/private/add")
    public ResponseEntity<List<String>> addPrivateChat(@AuthenticationPrincipal MyUserDetails userDetails, @RequestBody AddPrivateChatDTO privateChatDTO) {
        List<String> chatUsernames = userService.addPrivateChatToUser(new ObjectId(userDetails.getUserId()), privateChatDTO);
        return ResponseEntity.ok(chatUsernames);
    }

    @GetMapping("/groups")
    public ResponseEntity<List<GroupChatResponseDTO>> findGroupChats(@AuthenticationPrincipal MyUserDetails userDetails) {
        ArrayList<GroupChatResponseDTO> chatsDTOs = chatService.findGroupChats(new ObjectId(userDetails.getUserId()));
        return ResponseEntity.ok(chatsDTOs);
    }

    @GetMapping("/groups/{id}")
    public ResponseEntity<GroupChatResponseDTO> findGroupChat(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable("id") String groupId) {
        GroupChatResponseDTO chatsDTO = chatService.findById(new ObjectId(userDetails.getUserId()), new ObjectId(groupId));
        return ResponseEntity.ok(chatsDTO);
    }

    @PutMapping("/groups/{id}/host")
    public ResponseEntity<GroupChat> changeGroupChatHost(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable("id") String chatId, @RequestParam String newHostName) {
        GroupChat updatedGroupChat = chatService.changeGroupChatHost(new ObjectId(userDetails.getUserId()), new ObjectId(chatId), newHostName);
        return ResponseEntity.ok(updatedGroupChat);
    }

    @DeleteMapping("/groups/{id}")
    public ResponseEntity<Void> deleteGroupChat(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable("id") String chatId) {
        chatService.deleteGroupChat(new ObjectId(userDetails.getUserId()), new ObjectId(chatId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/groups/{id}/members")
    public ResponseEntity<List<String>> findGroupChatMemberNames(@PathVariable("id") String chatId) {
        ArrayList<String> chatsDTOs = chatService.findGroupChatMemberNames(new ObjectId(chatId));
        return ResponseEntity.ok(chatsDTOs);
    }

    @PutMapping("/groups/{id}/members/add")
    public ResponseEntity<GroupChat> addMemberToGroupChat(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable("id") String chatId, @RequestParam String memberName) {
        GroupChat updatedGroupChat = chatService.addMemberToGroupChat(new ObjectId(userDetails.getUserId()),
                new ObjectId(chatId), memberName);
        return ResponseEntity.ok(updatedGroupChat);
    }

    @PutMapping("/groups/{id}/members/remove")
    public ResponseEntity<GroupChat> removeMemberFromGroupChat(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable("id") String chatId, @RequestParam String memberName) {
        GroupChat updatedGroupChat = chatService.removeMemberFromGroupChat(new ObjectId(userDetails.getUserId()),
                new ObjectId(chatId), memberName);
        return ResponseEntity.ok(updatedGroupChat);
    }

    @PutMapping("/groups/{id}/name/update")
    public ResponseEntity<GroupChat> changeGroupChatName(@AuthenticationPrincipal MyUserDetails userDetails,
                                                         @PathVariable("id") String chatId,
                                                         @RequestParam String newName) {
        GroupChat updatedGroupChat = chatService.changeGroupChatName(new ObjectId(userDetails.getUserId()),
                new ObjectId(chatId), newName);
        return ResponseEntity.ok(updatedGroupChat);
    }

    @PostMapping("/groups/{id}/join")
    public ResponseEntity<Void> joinGroupChat(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable("id") String chatId) {
        chatService.addUserToGroup(new ObjectId(userDetails.getUserId()), new ObjectId(chatId));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/groups/{id}/leave")
    public ResponseEntity<Void> leaveGroupChat(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable("id") String chatId) {
        chatService.removeUserFromGroup(new ObjectId(userDetails.getUserId()), new ObjectId(chatId));
        return ResponseEntity.noContent().build();
    }
}