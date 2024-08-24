package com.example.chatsystem.controller;

import com.example.chatsystem.dto.MessageDTO;
import com.example.chatsystem.security.MyUserDetails;
import com.example.chatsystem.service.ChatService;
import com.example.chatsystem.service.MessageService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
