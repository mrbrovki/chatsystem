package com.example.chatsystem.service;

import com.example.chatsystem.model.Message;
import com.example.chatsystem.repository.MessageRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public List<Message> findAllMessages(String collectionName) {
        return messageRepository.findAllMessages(collectionName);
    }

    @Override
    public Message findMessageById(String collectionName, String id) {
        return messageRepository.findMessageById(collectionName, id);
    }

    @Override
    public Message saveMessage(String collectionName, Message message) {
        return messageRepository.saveMessage(collectionName, message);
    }

    @Override
    public Message updateMessage(String collectionName, Message message) {
        return messageRepository.updateMessage(collectionName, message);
    }

    @Override
    public void deleteMessage(String collectionName, String id) {
        messageRepository.deleteMessage(collectionName, id);
    }

    @Override
    public void deleteAllMessages(String collectionName) {
        messageRepository.deleteAllMessages(collectionName);
    }
}
