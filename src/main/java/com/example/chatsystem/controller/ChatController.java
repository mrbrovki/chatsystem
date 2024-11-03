package com.example.chatsystem.controller;

import com.example.chatsystem.dto.chat.*;
import com.example.chatsystem.dto.groupchat.CreateGroupRequest;
import com.example.chatsystem.model.GroupChat;
import com.example.chatsystem.security.MyUserDetails;
import com.example.chatsystem.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
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

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
        ChatResponseDTO chatsDTOs = chatService.findAllChats(new ObjectId(userDetails.getUserId()));
        return ResponseEntity.ok(chatsDTOs);
    }

    @DeleteMapping
    public void deleteChats(@AuthenticationPrincipal MyUserDetails userDetails, @RequestBody DeleteChatsRequest request) {
        chatService.deleteChats(new ObjectId(userDetails.getUserId()), request);
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

        GroupChatResponse createdGroupChat = chatService.createGroupChat(new ObjectId(userDetails.getUserId()), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGroupChat);
    }

    @GetMapping("/private")
    public ResponseEntity<List<PrivateChatResponse>> findPrivateChats(@AuthenticationPrincipal MyUserDetails userDetails) {
        ArrayList<PrivateChatResponse> chatsDTOs = chatService.findPrivateChats(new ObjectId(userDetails.getUserId()));
        return ResponseEntity.ok(chatsDTOs);
    }

    @GetMapping("/private/{username}")
    public ResponseEntity<PrivateChatResponse> findPrivateChatByName(@AuthenticationPrincipal MyUserDetails userDetails,
                                                                     @PathVariable String username) {
        return ResponseEntity.ok(chatService.findPrivateChatByName(new ObjectId(userDetails.getUserId()), username));
    }

    @PostMapping("/private/add")
    public ResponseEntity<Void> addPrivateChat(@AuthenticationPrincipal MyUserDetails userDetails,
                                                       @Valid  @RequestBody AddChatRequest addChatRequest) {
        chatService.addPrivateChat(new ObjectId(userDetails.getUserId()), addChatRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @DeleteMapping("private/delete")
    public ResponseEntity<Void> deletePrivateChat(@AuthenticationPrincipal MyUserDetails userDetails,
                                                                       @RequestParam String username,
                                                                       @RequestParam("isBoth") boolean isBoth) {
        chatService.deletePrivateChat(
                new ObjectId(userDetails.getUserId()), username, isBoth);
        return  ResponseEntity.noContent().build();
    }

    @GetMapping("/groups")
    public ResponseEntity<List<GroupChatResponse>> findGroupChats(@AuthenticationPrincipal MyUserDetails userDetails) {
        ArrayList<GroupChatResponse> chatsDTOs = chatService.findGroupChats(new ObjectId(userDetails.getUserId()));
        return ResponseEntity.ok(chatsDTOs);
    }

    @GetMapping("/groups/{id}")
    public ResponseEntity<GroupChatResponse> findGroupChat(@AuthenticationPrincipal MyUserDetails userDetails,
                                                           @PathVariable("id") String groupId) {
        GroupChatResponse chatsDTO = chatService.findById(new ObjectId(userDetails.getUserId()), new ObjectId(groupId));
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
    public ResponseEntity<Void> joinGroupChat(@AuthenticationPrincipal MyUserDetails userDetails,
                                              @PathVariable("id") String chatId) {
        chatService.addUserToGroup(new ObjectId(userDetails.getUserId()), new ObjectId(chatId));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/groups/{id}/leave")
    public ResponseEntity<Void> leaveGroupChat(@AuthenticationPrincipal MyUserDetails userDetails,
                                               @PathVariable("id") String chatId) {
        chatService.removeUserFromGroup(new ObjectId(userDetails.getUserId()), new ObjectId(chatId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bots")
    public ResponseEntity<List<BotChatResponse>> findBots(@AuthenticationPrincipal MyUserDetails userDetails) {
        ArrayList<BotChatResponse> bots = chatService.findBotChats(new ObjectId(userDetails.getUserId()));
        return ResponseEntity.ok(bots);
    }
}