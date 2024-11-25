package com.example.chatsystem.controller;

import com.example.chatsystem.config.websocket.aws.S3File;
import com.example.chatsystem.dto.message.MessageDTO;
import com.example.chatsystem.dto.message.MessagesResponse;
import com.example.chatsystem.model.ChatType;
import com.example.chatsystem.security.MyUserDetails;
import com.example.chatsystem.service.ChatService;
import com.example.chatsystem.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v4/messages")
public class MessageController {
    private final MessageService messageService;
    private final ChatService chatService;

    @Autowired
    public MessageController(MessageService messageService, ChatService chatService) {
        this.messageService = messageService;
        this.chatService = chatService;
    }

    @GetMapping
    public ResponseEntity<MessagesResponse> getAllMessages(@AuthenticationPrincipal MyUserDetails userDetails) {
        return ResponseEntity.ok(messageService.getAllMessages(userDetails));
    }

    @GetMapping(value = "/files/{id}")
    public ResponseEntity<byte[]> getImage(@AuthenticationPrincipal MyUserDetails userDetails,
                                           @PathVariable("id") String fileId,
                                           @RequestParam UUID chatId,
                                           @RequestParam UUID senderId,
                                           @RequestParam String chatType) {
        S3File file = messageService.findFileById(userDetails, chatId, senderId, ChatType.fromValue(chatType), fileId);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(file.getContentType().getValue())).body(file.getData());
    }

    @GetMapping("/private/{id}")
    public ResponseEntity<List<MessageDTO>> getPrivateMessages(@AuthenticationPrincipal MyUserDetails userDetails,
                                                               @PathVariable UUID id){
        List<MessageDTO> messageDTOS = messageService
                .getPrivateChatMessages(userDetails.getUserId(), id);
        return ResponseEntity.ok(messageDTOS);
    }

    @GetMapping("/bots/{id}")
    public ResponseEntity<List<MessageDTO>> getBotChatMessages(@AuthenticationPrincipal MyUserDetails userDetails,
                                                               @PathVariable UUID id){
        List<MessageDTO> messageDTOS = messageService
                .getBotChatMessages(userDetails.getUserId(), id);
        return ResponseEntity.ok(messageDTOS);
    }

    @GetMapping("/groups/{id}")
    public ResponseEntity<List<MessageDTO>> getGroupChatMessages(@AuthenticationPrincipal MyUserDetails userDetails,
                                                                 @PathVariable UUID id){
        List<MessageDTO> messageDTOS = messageService.getGroupChatMessages(userDetails.getUserId(),
                chatService.findById(id));
        return ResponseEntity.ok(messageDTOS);
    }

    @PutMapping("/private/{id}/status")
    public ResponseEntity<Void> updatePrivateReadStatus(@AuthenticationPrincipal MyUserDetails userDetails,
                                                          @PathVariable UUID id){
        messageService.updatePrivateReadStatus(userDetails.getUserId(), id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/groups/{id}/status")
    public ResponseEntity<Void> updateGroupReadStatus(@AuthenticationPrincipal MyUserDetails userDetails,
                                                        @PathVariable UUID id){
        messageService.updateGroupReadStatus(userDetails.getUserId(), id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/bots/{id}/status")
    public ResponseEntity<Void> updateBotReadStatus(@AuthenticationPrincipal MyUserDetails userDetails,
                                                      @PathVariable UUID id){
        messageService.updateBotReadStatus(userDetails.getUserId(), id);
        return ResponseEntity.noContent().build();
    }
}