package com.example.chatsystem.controller;

import com.example.chatsystem.config.websocket.aws.S3File;
import com.example.chatsystem.dto.MessageDTO;
import com.example.chatsystem.dto.MessagesDTO;
import com.example.chatsystem.model.ChatType;
import com.example.chatsystem.security.MyUserDetails;
import com.example.chatsystem.service.ChatService;
import com.example.chatsystem.service.MessageService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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

    @GetMapping("/")
    public ResponseEntity<MessagesDTO> getAllMessages(@AuthenticationPrincipal MyUserDetails userDetails) {
        System.out.println(userDetails.getUserId());
        return ResponseEntity.ok(messageService.getAllMessages(userDetails));
    }

    @GetMapping(value = "/files/{id}")
    public ResponseEntity<byte[]> getImage(@AuthenticationPrincipal MyUserDetails userDetails,
                                           @PathVariable("id") String fileId,
                                           @RequestParam String chatName,
                                           @RequestParam String senderName,
                                           @RequestParam String chatType) {
        S3File file = messageService.findFileById(userDetails, chatName, senderName, ChatType.fromValue(chatType), fileId);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(file.getContentType().getValue())).body(file.getData());
    }

    @GetMapping("/users/{username}")
    public ResponseEntity<List<MessageDTO>> getPrivateMessages(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable String username){
        List<MessageDTO> messageDTOS = messageService.getPrivateChatMessages(userDetails, username);
        return ResponseEntity.ok(messageDTOS);
    }

    @GetMapping("/bots/{botName}")
    public ResponseEntity<List<MessageDTO>> getBotChatMessages(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable String botName){
        List<MessageDTO> messageDTOS = messageService.getBotChatMessages(userDetails, botName);
        return ResponseEntity.ok(messageDTOS);
    }


    @GetMapping("/groups/{groupId}")
    public ResponseEntity<List<MessageDTO>> getGroupChatMessages(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable String groupId){
        List<MessageDTO> messageDTOS = messageService.getGroupChatMessages(userDetails, chatService.findById(new ObjectId(groupId)));
        return ResponseEntity.ok(messageDTOS);
    }
}