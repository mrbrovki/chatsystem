package com.example.chatsystem.controller;

import com.example.chatsystem.model.Message;
import com.example.chatsystem.service.MessageService;
import com.example.chatsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {
    private MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {

        this.messageService = messageService;
    }

    @GetMapping
    public List<Message> findAllMessages(@RequestParam("collection") String collectionName) {
        return messageService.findAllMessages(collectionName);
    }

    @GetMapping("/{id}")
    public Message findMessageById(@RequestParam("collection") String collectionName, @PathVariable String id) {
        return messageService.findMessageById(collectionName, id);
    }

    @PostMapping
    public Message saveMessage(@RequestParam("collection") String collectionName, @RequestBody Message message) {
        return messageService.saveMessage(collectionName, message);
    }

    @PutMapping("/{id}")
    public Message updateMessage(@RequestParam("collection") String collectionName, @PathVariable String id, @RequestBody Message message) {
        message.setId(id);
        return messageService.updateMessage(collectionName, message);
    }

    @DeleteMapping("/{id}")
    public void deleteMessage(@RequestParam("collection") String collectionName, @PathVariable String id) {
        messageService.deleteMessage(collectionName, id);
    }

    @DeleteMapping
    public void deleteAllMessages(@RequestParam("collection") String collectionName) {
        messageService.deleteAllMessages(collectionName);
    }
}
