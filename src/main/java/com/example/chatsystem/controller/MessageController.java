package com.example.chatsystem.controller;

import com.example.chatsystem.config.websocket.aws.S3File;
import com.example.chatsystem.dto.ImageRequestDTO;
import com.example.chatsystem.dto.MessageDTO;
import com.example.chatsystem.security.MyUserDetails;
import com.example.chatsystem.service.ChatService;
import com.example.chatsystem.service.MessageService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v2/messages")
public class MessageController {
    private final MessageService messageService;
    private final ChatService chatService;

    @Autowired
    public MessageController(MessageService messageService, ChatService chatService) {
        this.messageService = messageService;
        this.chatService = chatService;
    }

    @GetMapping("/")
    public ResponseEntity<List<MessageDTO>> getAllMessages(@AuthenticationPrincipal MyUserDetails user) {
        List<MessageDTO> messageDTOS = new ArrayList<>();
        return ResponseEntity.ok(messageDTOS);
    }

    @PostMapping(value = "/images/{id}")
    public ResponseEntity<byte[]> getImage(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable("id") String imageId, @RequestBody ImageRequestDTO imageRequestDTO) {
        S3File file = messageService.findImageById(userDetails, imageRequestDTO, imageId);
        return ResponseEntity.ok().contentType(file.getContentType()).body(file.getData());
    }

    @GetMapping("/{username}")
    public ResponseEntity<List<MessageDTO>> getMessages(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable String username){
        List<MessageDTO> messageDTOS = messageService.getChatMessages(userDetails, username);
        return ResponseEntity.ok(messageDTOS);
    }

    @GetMapping("/groups/{groupId}")
    public ResponseEntity<List<MessageDTO>> getGroupChatMessages(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable String groupId){
        List<MessageDTO> messageDTOS = messageService.getGroupChatMessages(userDetails, chatService.findById(new ObjectId(groupId)));
        return ResponseEntity.ok(messageDTOS);
    }
}