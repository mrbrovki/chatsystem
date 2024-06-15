package com.example.chatsystem.service.impl;

import com.example.chatsystem.dto.MessageDTO;
import com.example.chatsystem.dto.MessageReceiveDTO;
import com.example.chatsystem.dto.MessageSendDTO;
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
            }
            case GROUP -> collectionName = "group_" + messageSendDTO.getReceiverName();
            default -> collectionName = "";
        }

        message.setId(sender.getUserId().toHexString()+messageReceiveDTO.getTimestamp());
        message.setSenderId(sender.getUserId());
        message.setMessage(messageReceiveDTO.getMessage());
        message.setType(messageReceiveDTO.getType());
        message.setTimestamp(messageReceiveDTO.getTimestamp());
        saveMessage(collectionName, message);
    }

    @Override
    public List<MessageDTO> getChatMessages(MyUserDetails userDetails, String targetUserName){
        ObjectId targetUserId = userService.findByUsername(targetUserName).getUserId();
        String chatCollection = ChatServiceImpl.getPrivateChatCollectionName(new ObjectId(userDetails.getUserId()), targetUserId);
        List<Message> messages = messageRepository.findAllMessages(chatCollection);
        List<MessageDTO> messageDTOS = new ArrayList<>();
        for (Message message : messages) {
            MessageDTO messageDTO = MessageDTO.builder()
                    .message(message.getMessage())
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

        List<Message> messages = messageRepository.findAllMessages("group_"+groupChat.getId().toHexString());

        for (Message message : messages) {
            MessageDTO messageDTO = MessageDTO.builder()
                    .message(message.getMessage())
                    .senderName(userService.findById(message.getSenderId()).getEmail())
                    .timestamp(message.getTimestamp())
                    .build();
            messageDTOS.add(messageDTO);
        }
        return messageDTOS;
    }
}
