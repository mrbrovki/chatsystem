package com.example.chatsystem.service.impl;

import com.example.chatsystem.dto.MessageDTO;
import com.example.chatsystem.dto.MessageReceiveDTO;
import com.example.chatsystem.dto.MessageSendDTO;
import com.example.chatsystem.exception.DocumentNotFoundException;
import com.example.chatsystem.model.GroupChat;
import com.example.chatsystem.model.Message;
import com.example.chatsystem.model.MessageType;
import com.example.chatsystem.model.User;
import com.example.chatsystem.repository.MessageRepository;
import com.example.chatsystem.security.MyUserDetails;
import com.example.chatsystem.service.MessageService;
import com.example.chatsystem.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;

    private final UserService userService;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository, UserService userService) {
        this.messageRepository = messageRepository;
        this.userService = userService;
    }

    @Override
    public List<Message> findAllMessages(String collectionName) {
        return messageRepository.findAll(collectionName);
    }

    @Override
    public Message findMessageById(String collectionName, ObjectId id) {
        return messageRepository.findById(collectionName, id).orElseThrow(()->
                new DocumentNotFoundException("Message " + id + " not found in collection " + collectionName));
    }

    @Override
    public Message saveMessage(String collectionName, Message message) {
        return messageRepository.save(collectionName, message);
    }

    @Override
    public Message updateMessage(String collectionName, Message message) {
        return messageRepository.update(collectionName, message);
    }

    @Override
    public void deleteMessage(String collectionName, ObjectId id) {
        Message message = findMessageById(collectionName, id);
        messageRepository.delete(collectionName, message);
    }

    @Override
    public void deleteAllMessages(String collectionName) {
        messageRepository.deleteAll(collectionName);
    }

    @Override
    public void persistMessage(MessageSendDTO messageSendDTO, MessageReceiveDTO messageReceiveDTO, MessageType messageType){
        Message message = new Message();
        //  persist
        User sender = userService.findByUsername(messageReceiveDTO.getSenderName());

        String collectionName;

        switch (messageType){
            case PRIVATE -> {
                User receiver = userService.findByUsername(messageSendDTO.getReceiverName());
                message.setReceiverId(receiver.getUserId());
                collectionName = ChatServiceImpl.getPrivateChatCollectionName(sender.getUserId(), receiver.getUserId());

                if(!collectionExists(collectionName)){
                    if(!sender.getChats().contains(receiver.getUserId())){
                        userService.addPrivateChatToUser(sender, receiver.getUserId());
                    }
                    if(!receiver.getChats().contains(sender.getUserId())){
                        userService.addPrivateChatToUser(receiver, sender.getUserId());
                    }
                }

            }
            case GROUP -> collectionName = "group_" + messageSendDTO.getReceiverName();
            default -> collectionName = "";
        }

        message.setId(sender.getUserId().toHexString()+messageReceiveDTO.getTimestamp());
        message.setSenderId(sender.getUserId());
        message.setContent(messageReceiveDTO.getContent());
        message.setType(messageReceiveDTO.getType());
        message.setTimestamp(messageReceiveDTO.getTimestamp());
        saveMessage(collectionName, message);
    }

    @Override
    public boolean collectionExists(String collectionName) {
        return messageRepository.collectionExists(collectionName);
    }

    @Override
    public List<MessageDTO> getChatMessages(MyUserDetails userDetails, String targetUserName){
        ObjectId targetUserId = userService.findByUsername(targetUserName).getUserId();
        String chatCollection = ChatServiceImpl.getPrivateChatCollectionName(new ObjectId(userDetails.getUserId()), targetUserId);
        List<Message> messages = messageRepository.findAll(chatCollection);
        List<MessageDTO> messageDTOS = new ArrayList<>();
        for (Message message : messages) {
            MessageDTO messageDTO = MessageDTO.builder()
                    .content(message.getContent())
                    .timestamp(message.getTimestamp())
                    .build();
            if(message.getSenderId().toHexString().equals(userDetails.getUserId())){
                messageDTO.setSenderName(userDetails.getUsername());
            }else{
                messageDTO.setSenderName(targetUserName);
            }
            messageDTOS.add(messageDTO);
        }
        return messageDTOS;
    }

    @Override
    public List<MessageDTO> getGroupChatMessages(MyUserDetails userDetails, GroupChat groupChat){
        List<MessageDTO> messageDTOS = new ArrayList<>();

        if(!groupChat.getMemberIds().contains(new ObjectId(userDetails.getUserId()))){
            return messageDTOS;
        }

        List<Message> messages = messageRepository.findAll("group_"+groupChat.getId().toHexString());

        for (Message message : messages) {
            MessageDTO messageDTO = MessageDTO.builder()
                    .content(message.getContent())
                    .senderName(userService.findById(message.getSenderId()).getUsername())
                    .timestamp(message.getTimestamp())
                    .build();
            messageDTOS.add(messageDTO);
        }
        return messageDTOS;
    }

    @Override
    public MessageReceiveDTO buildMessageReceiveDTO(MessageSendDTO messageSendDTO, String senderName) {
        Instant instant = Instant.now();
        return MessageReceiveDTO.builder()
                .timestamp(instant.toEpochMilli())
                .senderName(senderName)
                .content(messageSendDTO.getContent())
                .type(messageSendDTO.getType())
                .build();
    }
}
