package com.example.chatsystem.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.chatsystem.dto.ChatResponseDTO;
import com.example.chatsystem.dto.AddPrivateChatDTO;
import com.example.chatsystem.dto.GroupChatCreateDTO;
import com.example.chatsystem.model.GroupChat;
import com.example.chatsystem.security.MyUserDetails;
import com.example.chatsystem.service.ChatService;
import com.example.chatsystem.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    private final AmazonS3 amazonS3;

    @Autowired
    public ChatController(ChatService chatService, UserService userService, AmazonS3 amazonS3) {
        this.chatService = chatService;
        this.userService = userService;
        this.amazonS3 = amazonS3;
    }

    @GetMapping
    public ResponseEntity<List<ChatResponseDTO>> findAllChats(@AuthenticationPrincipal MyUserDetails userDetails) {
        ArrayList<ChatResponseDTO> chatsDTOs = chatService.findAllChats(new ObjectId(userDetails.getUserId()));
        return ResponseEntity.ok(chatsDTOs);
    }


    @PostMapping("/groups")
    public ResponseEntity<ChatResponseDTO> createGroupChat(@AuthenticationPrincipal MyUserDetails userDetails,
                                                           @RequestPart("json") GroupChatCreateDTO groupChatCreateDTO,
                                                           @RequestPart("avatar") MultipartFile avatarFile) {
        try {
            InputStream inputStream = avatarFile.getInputStream();
            ObjectMetadata objectMetadata = new ObjectMetadata();
            PutObjectRequest putObjectRequest = new PutObjectRequest("chatbucket69", userDetails.getUserId()+"avatar.jpg", inputStream, objectMetadata);
            amazonS3.putObject(putObjectRequest);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ChatResponseDTO createdGroupChat = chatService.createGroupChat(new ObjectId(userDetails.getUserId()), groupChatCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGroupChat);
    }

    @GetMapping("/private")
    public ResponseEntity<List<ChatResponseDTO>> findPrivateChats(@AuthenticationPrincipal MyUserDetails userDetails) {
        ArrayList<ChatResponseDTO> chatsDTOs = chatService.findPrivateChats(new ObjectId(userDetails.getUserId()));
        return ResponseEntity.ok(chatsDTOs);
    }

    @GetMapping("/groups")
    public ResponseEntity<List<ChatResponseDTO>> findGroupChats(@AuthenticationPrincipal MyUserDetails userDetails) {
        ArrayList<ChatResponseDTO> chatsDTOs = chatService.findGroupChats(new ObjectId(userDetails.getUserId()));
        return ResponseEntity.ok(chatsDTOs);
    }

    @GetMapping("/groups/{id}")
    public ResponseEntity<ChatResponseDTO> findGroupChat(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable("id") String groupId) {
        ChatResponseDTO chatsDTO = chatService.findById(new ObjectId(userDetails.getUserId()), new ObjectId(groupId));
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

    @PutMapping("/private/add")
    public ResponseEntity<List<String>> addPrivateChat(@AuthenticationPrincipal MyUserDetails userDetails, @RequestBody AddPrivateChatDTO privateChatDTO) {
        List<String> chatUsernames = userService.addPrivateChatToUser(new ObjectId(userDetails.getUserId()), privateChatDTO);
        return ResponseEntity.ok(chatUsernames);
    }
}
