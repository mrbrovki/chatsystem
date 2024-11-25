package com.example.chatsystem.controller;

import com.example.chatsystem.dto.chat.*;
import com.example.chatsystem.dto.groupchat.CreateGroupRequest;
import com.example.chatsystem.model.GroupChat;
import com.example.chatsystem.security.MyUserDetails;
import com.example.chatsystem.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v4/chats")
public class ChatController {
    private final ChatService chatService;
    private final Validator validator;

    @Autowired
    public ChatController(ChatService chatService, Validator validator) {
        this.chatService = chatService;
        this.validator = validator;
    }

    @GetMapping
    public ResponseEntity<ChatResponseDTO> findAllChats(@AuthenticationPrincipal MyUserDetails userDetails) {
        ChatResponseDTO chatsDTOs = chatService.findAllChats(userDetails.getUserId());
        return ResponseEntity.ok(chatsDTOs);
    }

    @DeleteMapping
    public void deleteChats(@AuthenticationPrincipal MyUserDetails userDetails, @RequestBody DeleteChatsRequest request) {
        chatService.deleteChats(userDetails.getUserId(), request);
    }


    //  PRIVATE
    @GetMapping("/private")
    public ResponseEntity<List<PrivateChatResponse>> findPrivateChats(@AuthenticationPrincipal MyUserDetails userDetails) {
        ArrayList<PrivateChatResponse> chatsDTOs = chatService.findPrivateChats(userDetails.getUserId());
        return ResponseEntity.ok(chatsDTOs);
    }

    @PostMapping("/private/add")
    public ResponseEntity<Void> addPrivateChat(@AuthenticationPrincipal MyUserDetails userDetails,
                                               @Valid  @RequestBody AddChatRequest addChatRequest) {
        chatService.addPrivateChat(userDetails.getUserId(), addChatRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/private/delete")
    public ResponseEntity<Void> deletePrivateChat(@AuthenticationPrincipal MyUserDetails userDetails,
                                                  @RequestParam UUID id,
                                                  @RequestParam("isBoth") boolean isBoth) {
        chatService.deletePrivateChat(
                userDetails.getUserId(), id, isBoth);
        return  ResponseEntity.noContent().build();
    }

    @GetMapping("/private/{id}")
    public ResponseEntity<PrivateChatResponse> findPrivateChatById(@AuthenticationPrincipal MyUserDetails userDetails,
                                                                     @PathVariable UUID id) {
        return ResponseEntity.ok(chatService.findPrivateChatById(userDetails.getUserId(), id));
    }


    //  GROUP
    @GetMapping("/groups")
    public ResponseEntity<List<GroupChatResponse>> findGroupChats(@AuthenticationPrincipal MyUserDetails userDetails) {
        ArrayList<GroupChatResponse> chatsDTOs = chatService.findGroupChats(userDetails.getUserId());
        return ResponseEntity.ok(chatsDTOs);
    }

    @PostMapping(value = "/groups", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GroupChatResponse> createGroupChat(@AuthenticationPrincipal MyUserDetails userDetails,
                                                             @RequestPart("json") String json,
                                                             @RequestPart("image") MultipartFile image) {
        CreateGroupRequest request;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            request = objectMapper.readValue(json,  CreateGroupRequest.class);
            Errors errors = validator.validateObject(request);
            if(errors.hasErrors()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            request.setImage(image);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        GroupChatResponse createdGroupChat = chatService.createGroupChat(userDetails.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGroupChat);
    }

    @GetMapping("/groups/{id}")
    public ResponseEntity<GroupChatResponse> findGroupChat(@AuthenticationPrincipal MyUserDetails userDetails,
                                                           @PathVariable("id") UUID groupId) {
        GroupChatResponse chatsDTO = chatService.findById(userDetails.getUserId(), groupId);
        return ResponseEntity.ok(chatsDTO);
    }

    @PutMapping("/groups/{id}/host")
    public ResponseEntity<GroupChat> changeGroupChatHost(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable("id") UUID groupId, @RequestParam String newHostName) {
        GroupChat updatedGroupChat = chatService.changeGroupChatHost(userDetails.getUserId(), groupId, newHostName);
        return ResponseEntity.ok(updatedGroupChat);
    }

    @DeleteMapping("/groups/{id}")
    public ResponseEntity<Void> deleteGroupChat(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable("id") UUID groupId) {
        chatService.deleteGroupChat(userDetails.getUserId(), groupId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/groups/{id}/members")
    public ResponseEntity<List<String>> findGroupChatMemberNames(@PathVariable("id") UUID groupId) {
        ArrayList<String> chatsDTOs = chatService.findGroupChatMemberNames(groupId);
        return ResponseEntity.ok(chatsDTOs);
    }

    @PutMapping("/groups/{id}/members/add")
    public ResponseEntity<GroupChat> addMemberToGroupChat(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable("id") UUID groupId, @RequestParam String memberName) {
        GroupChat updatedGroupChat = chatService.addMemberToGroupChat(userDetails.getUserId(),
                groupId, memberName);
        return ResponseEntity.ok(updatedGroupChat);
    }

    @PutMapping("/groups/{id}/members/remove")
    public ResponseEntity<GroupChat> removeMemberFromGroupChat(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable("id") UUID groupId, @RequestParam String memberName) {
        GroupChat updatedGroupChat = chatService.removeMemberFromGroupChat(userDetails.getUserId(),
                groupId, memberName);
        return ResponseEntity.ok(updatedGroupChat);
    }

    @PutMapping("/groups/{id}/name/update")
    public ResponseEntity<GroupChat> changeGroupChatName(@AuthenticationPrincipal MyUserDetails userDetails,
                                                         @PathVariable("id") UUID groupId,
                                                         @RequestParam String newName) {
        GroupChat updatedGroupChat = chatService.changeGroupChatName(userDetails.getUserId(),
                groupId, newName);
        return ResponseEntity.ok(updatedGroupChat);
    }

    @PostMapping("/groups/{id}/join")
    public ResponseEntity<Void> joinGroupChat(@AuthenticationPrincipal MyUserDetails userDetails,
                                              @PathVariable("id") UUID groupId) {
        chatService.addUserToGroup(userDetails.getUserId(), groupId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/groups/{id}/leave")
    public ResponseEntity<Void> leaveGroupChat(@AuthenticationPrincipal MyUserDetails userDetails,
                                               @PathVariable("id") UUID groupId) {
        chatService.removeUserFromGroup(userDetails.getUserId(), groupId);
        return ResponseEntity.noContent().build();
    }


    //  BOT
    @GetMapping("/bots")
    public ResponseEntity<List<BotChatResponse>> findBots(@AuthenticationPrincipal MyUserDetails userDetails) {
        ArrayList<BotChatResponse> bots = chatService.findBotChats(userDetails.getUserId());
        return ResponseEntity.ok(bots);
    }

    @DeleteMapping("/bots/delete")
    public ResponseEntity<Void> deleteBot(@AuthenticationPrincipal MyUserDetails userDetails, @RequestParam UUID id) {
        chatService.deleteBotChat(userDetails.getUserId(), id);
        return ResponseEntity.noContent().build();
    }
}