package com.example.chatsystem.service.impl;

import com.amazonaws.services.s3.model.PutObjectResult;
import com.example.chatsystem.bot.Bot;
import com.example.chatsystem.bot.BotService;
import com.example.chatsystem.config.websocket.aws.S3File;
import com.example.chatsystem.dto.message.MessageDTO;
import com.example.chatsystem.dto.websocket.MessageReceiveDTO;
import com.example.chatsystem.dto.message.MessagesResponse;
import com.example.chatsystem.exception.DocumentNotFoundException;
import com.example.chatsystem.model.*;
import com.example.chatsystem.repository.GroupChatRepo;
import com.example.chatsystem.repository.MessageRepository;
import com.example.chatsystem.security.MyUserDetails;
import com.example.chatsystem.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

import static com.example.chatsystem.utils.CollectionUtils.buildCollectionName;

@Service
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final S3Service s3Service;
    private final UserService userService;
    private final BotService botService;
    private final ReadStatusService readStatusService;
    private final GroupChatRepo groupChatRepo;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository, S3Service s3Service, UserService userService,
                              BotService botService, ReadStatusService readStatusService, GroupChatRepo groupChatRepo) {
        this.messageRepository = messageRepository;
        this.s3Service = s3Service;
        this.userService = userService;
        this.botService = botService;
        this.readStatusService = readStatusService;
        this.groupChatRepo = groupChatRepo;
    }

    @Override
    public List<Message> findAllMessages(String collectionName) {
        return messageRepository.findAll(collectionName);
    }

    @Override
    public List<Message> findAfter(String messageId, String collectionName) {
        return messageRepository.findAfter(messageId, collectionName);
    }

    @Override
    public Message findMessageById(String collectionName, String id) {
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
    public void deleteMessage(String collectionName, String id) {
        Message message = findMessageById(collectionName, id);
        messageRepository.delete(collectionName, message);
    }

    @Override
    public void deleteAllMessages(String collectionName) {
        messageRepository.deleteAll(collectionName);
    }

    @Override
    public void persistPrivateMessage(MessageReceiveDTO messageReceiveDTO, UUID senderId, UUID receiverId) {
        String collectionName = buildCollectionName(senderId, receiverId);

        if(!collectionExists(collectionName)) {
            userService.addPrivateChatToUser(senderId, receiverId);
        }
        userService.addPrivateChatToUser(receiverId, senderId);

        Message message = persistMessage(collectionName, senderId, messageReceiveDTO);
        readStatusService.persist(senderId, collectionName);
    }

    @Override
    public void persistPrivateMessage(MessageReceiveDTO messageReceiveDTO, String collectionName,
                                      UUID senderId, UUID receiverId) {
        if(!collectionExists(collectionName)) {
            userService.addPrivateChatToUser(senderId, receiverId);
            userService.addPrivateChatToUser(receiverId, senderId);
        }

        Message message = persistMessage(collectionName, senderId, messageReceiveDTO);
        readStatusService.persist(senderId, collectionName);
    }

    @Override
    public void persistBotMessage(MessageReceiveDTO messageReceiveDTO, UUID senderId, UUID receiverId) {
        String collectionName = buildCollectionName(senderId, receiverId);

        Message message =  persistMessage(collectionName, senderId, messageReceiveDTO);
        readStatusService.persist(senderId, collectionName);
    }

    @Override
    public void persistBotMessage(MessageReceiveDTO messageReceiveDTO, String collectionName,
                                  UUID senderId, UUID receiverId) {
        Message message = persistMessage(collectionName, senderId, messageReceiveDTO);
        readStatusService.persist(senderId, collectionName);
    }

    @Override
    public void persistGroupMessage(MessageReceiveDTO messageReceiveDTO, UUID senderId, UUID groupId) {
        String collectionName = buildCollectionName(groupId, null);

        Message message = persistMessage(collectionName, senderId, messageReceiveDTO);
        readStatusService.persist(senderId, collectionName);
    }

    @Override
    public void persistGroupMessage(MessageReceiveDTO messageReceiveDTO, String collectionName,
                                    UUID senderId) {
        Message message = persistMessage(collectionName, senderId, messageReceiveDTO);
        readStatusService.persist(senderId, collectionName);
    }

    @Override
    public void persistGroupFile(MessageReceiveDTO messageReceiveDTO, InputStream inputStream, UUID senderId, UUID groupId) {
        String collectionName = buildCollectionName(groupId, null);

        //  upload to s3
        String fileId = senderId.toString()+messageReceiveDTO.getTimestamp();
        String key = collectionName + "/" + fileId;
        PutObjectResult result = s3Service.uploadChatFile(inputStream, messageReceiveDTO.getType(), key);

        //  persist
        persistGroupMessage(messageReceiveDTO, collectionName, senderId);
    }

    @Override
    public void persistBotFile(MessageReceiveDTO messageReceiveDTO, InputStream inputStream, UUID senderId, UUID receiverId){
        String collectionName = buildCollectionName(senderId, receiverId);

        //upload to s3
        String fileId = senderId.toString()+messageReceiveDTO.getTimestamp();
        String key = collectionName + "/" + fileId;
        PutObjectResult result = s3Service.uploadChatFile(inputStream, messageReceiveDTO.getType(), key);

        //  persist
        persistBotMessage(messageReceiveDTO, collectionName, senderId, receiverId);
    }

    @Override
    public void persistPrivateFile(MessageReceiveDTO messageReceiveDTO, InputStream inputStream,
                                   UUID senderId, UUID receiverId) {
        String collectionName = buildCollectionName(senderId, receiverId);

        //upload to s3
        String fileId = senderId.toString()+messageReceiveDTO.getTimestamp();
        String key = collectionName + "/" + fileId;
        PutObjectResult result = s3Service.uploadChatFile(inputStream, messageReceiveDTO.getType(), key);

        //  persist
        persistPrivateMessage(messageReceiveDTO, collectionName, senderId, receiverId);
    }

    private Message persistMessage(String collectionName, UUID senderId, MessageReceiveDTO messageReceiveDTO) {
        Message message = new Message();
        message.setId(senderId.toString()+messageReceiveDTO.getTimestamp());
        message.setSenderId(senderId);
        message.setContent(messageReceiveDTO.getContent());
        message.setType(messageReceiveDTO.getType());
        message.setTimestamp(messageReceiveDTO.getTimestamp());
        return saveMessage(collectionName, message);
    }


    @Override
    public boolean collectionExists(String collectionName) {
        return messageRepository.collectionExists(collectionName);
    }

    @Override
    public MessagesResponse getAllMessages(MyUserDetails userDetails){
        User user = userService.findById(userDetails.getUserId());

        List<UUID> privateChats = user.getPrivateChats();
        HashMap<UUID, List<MessageDTO>> privateHM = new HashMap<>();

        for (UUID privateChatId : privateChats) {
            User targetUser = userService.findById(privateChatId);
            privateHM.put(targetUser.getId(), getPrivateChatMessages(userDetails.getUserId(), targetUser.getId()));
        }

        List<UUID> botChats = user.getBotChats();
        HashMap<UUID, List<MessageDTO>> botsHM = new HashMap<>();

        for (UUID botChatId : botChats) {
            Bot bot = botService.getBotById(botChatId);
            botsHM.put(bot.getId(), getBotChatMessages(userDetails.getUserId(), botChatId));
        }

        List<UUID> groupChats = user.getGroupChats();
        HashMap<UUID, List<MessageDTO>> groupsHM = new HashMap<>();
        for (UUID groupChatId : groupChats) {
            GroupChat groupChat = groupChatRepo.findById(groupChatId).orElseThrow(()->
                    new DocumentNotFoundException("Group " + groupChatId.toString() + " not found"));
            groupsHM.put(groupChat.getId(), getGroupChatMessages(userDetails.getUserId(), groupChat));
        }

        return MessagesResponse.builder()
                .PRIVATE(privateHM)
                .GROUP(groupsHM)
                .BOT(botsHM)
                .build();
    }

    @Override
    public List<MessageDTO> getPrivateChatMessages(UUID userId, UUID targetUserId){
        String chatCollection = buildCollectionName(userId, targetUserId);
        return getCollectionMessages(userId, targetUserId, chatCollection);
    }

    @Override
    public List<MessageDTO> getBotChatMessages(UUID userId, UUID botId){
        String chatCollection = buildCollectionName(userId, botId);
        return getCollectionMessages(userId, botId, chatCollection);
    }

    @Override
    public List<MessageDTO> getGroupChatMessages(UUID userId, GroupChat groupChat){
        List<MessageDTO> messageDTOS = new ArrayList<>();

        if(!groupChat.getMemberIds().contains(userId)){
            return messageDTOS;
        }

        String chatCollection = buildCollectionName(groupChat.getId(), null);
        List<Message> messages = messageRepository.findAll(chatCollection);

        for (Message message : messages) {
            MessageDTO messageDTO = MessageDTO.builder()
                    .type(message.getType())
                    .content(message.getContent())
                    .senderId(message.getSenderId())
                    .timestamp(message.getTimestamp())
                    .build();
            messageDTOS.add(messageDTO);
        }
        return messageDTOS;
    }

    private List<MessageDTO> getCollectionMessages(UUID userId,  UUID chatId, String collectionName){
        ReadStatus readStatus = readStatusService.getReadStatus(collectionName, userId);
        List<Message> messages = null;
        if (readStatus.getLastMessage() != null) {
            messages = messageRepository.findAfter(readStatus.getLastMessage(), collectionName);
        }else{
            messages = messageRepository.findAll(collectionName);
        }

        List<MessageDTO> messageDTOS = new ArrayList<>();
        for (Message message : messages) {
            MessageDTO messageDTO = MessageDTO.builder()
                    .type(message.getType())
                    .content(message.getContent())
                    .timestamp(message.getTimestamp())
                    .build();
            if(message.getSenderId().equals(userId)){
                messageDTO.setSenderId(userId);
            }else{
                messageDTO.setSenderId(chatId);
            }
            messageDTOS.add(messageDTO);
        }
        return messageDTOS;
    }


    @Override
    public S3File findFileById(MyUserDetails userDetails, UUID chatId,
                                UUID senderId, ChatType chatType, String fileId) {
        String key;
        UUID userId = userDetails.getUserId();

        switch (chatType){
            case PRIVATE, BOT:
                if(userDetails.getUserId().equals(senderId)){
                    senderId = userId;
                }else{
                    senderId = chatId;
                }
                key = buildCollectionName(userId, chatId) + "/" + senderId + fileId;
                break;
            case GROUP:
                        GroupChat groupChat = groupChatRepo.findById(chatId)
                                .orElseThrow(()->
                                        new DocumentNotFoundException("Group " + chatId + " not found"));
                        List<UUID> memberIds = groupChat.getMemberIds();
                        for (UUID memberId : memberIds) {
                            if(memberId.equals(senderId)){
                                senderId = memberId;
                            }
                        }
                        key = buildCollectionName(groupChat.getId(), null) + "/" + senderId + fileId;
                 break;
            default:
                key = "dummy";
                 break;
        }
        return s3Service.getChatFile(key);
    }


    @Override
    public void updateLastMessageStatus(UUID userId, String collectionName) {
        Optional<Message> lastMessage = messageRepository.findLastMessage(collectionName);
        lastMessage.ifPresent(message -> readStatusService.updateLastMessage(collectionName, message.getId(), userId));
    }

    @Override
    public void updatePrivateReadStatus(UUID userId, UUID targetUserId) {
        String collectionName = buildCollectionName(userId, targetUserId);
        readStatusService.updateTimeRead(collectionName, userId);
    }

    @Override
    public void updateGroupReadStatus(UUID userId, UUID groupId) {
        String collectionName = buildCollectionName(groupId, null);
        readStatusService.updateTimeRead(collectionName, userId);
    }

    @Override
    public void updateBotReadStatus(UUID userId, UUID botId) {
        String collectionName = buildCollectionName(userId, botId);
        readStatusService.updateTimeRead(collectionName, botId);
    }
}
