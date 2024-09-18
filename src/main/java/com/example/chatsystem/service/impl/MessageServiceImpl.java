package com.example.chatsystem.service.impl;

import com.amazonaws.services.s3.model.PutObjectResult;
import com.example.chatsystem.bot.Bot;
import com.example.chatsystem.bot.BotService;
import com.example.chatsystem.config.websocket.aws.S3File;
import com.example.chatsystem.dto.*;
import com.example.chatsystem.exception.DocumentNotFoundException;
import com.example.chatsystem.model.*;
import com.example.chatsystem.repository.MessageRepository;
import com.example.chatsystem.security.MyUserDetails;
import com.example.chatsystem.service.*;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final S3Service s3Service;
    private final UserService userService;
    private final BotService botService;
    private final ReadStatusService readStatusService;
    private final GroupChatService groupChatService;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository, S3Service s3Service, UserService userService,
                              BotService botService, ReadStatusService readStatusService, GroupChatService groupChatService                              ) {
        this.messageRepository = messageRepository;
        this.s3Service = s3Service;
        this.userService = userService;
        this.botService = botService;
        this.readStatusService = readStatusService;
        this.groupChatService = groupChatService;
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
    public void persistPrivateMessage(MessageSendDTO messageSendDTO, MessageReceiveDTO messageReceiveDTO) {
        Message message = new Message();

        User sender = userService.findByUsername(messageReceiveDTO.getSenderName());
        User receiver = userService.findByUsername(messageSendDTO.getReceiverName());
        message.setReceiverId(receiver.getUserId());
        String collectionName = buildCollectionName(sender.getUserId(), receiver.getUserId(), ChatType.PRIVATE);

        if(!collectionExists(collectionName)) {
            if (!sender.getPrivateChats().contains(receiver.getUserId())) {
                userService.addPrivateChatToUser(sender, receiver.getUserId());
            }
            if (!receiver.getPrivateChats().contains(sender.getUserId())) {
                userService.addPrivateChatToUser(receiver, sender.getUserId());
            }
        }

        message.setId(sender.getUserId().toHexString()+messageReceiveDTO.getTimestamp());
        message.setSenderId(sender.getUserId());
        message.setContent(messageReceiveDTO.getContent());
        message.setType(messageReceiveDTO.getType());
        message.setTimestamp(messageReceiveDTO.getTimestamp());
        saveMessage(collectionName, message);

        readStatusService.createReadStatus(collectionName, message, List.of(sender.getUserId(), receiver.getUserId()));
    }

    @Override
    public void persistBotMessage(MessageReceiveDTO messageReceiveDTO, ObjectId senderId, ObjectId receiverId) {
        Message message = new Message();
        String collectionName = buildCollectionName(senderId, receiverId, ChatType.BOT);

        message.setId(senderId.toHexString()+messageReceiveDTO.getTimestamp());
        message.setSenderId(senderId);
        message.setContent(messageReceiveDTO.getContent());
        message.setType(MessageType.TEXT);
        message.setTimestamp(messageReceiveDTO.getTimestamp());
        saveMessage(collectionName, message);

        readStatusService.createReadStatus(collectionName, message, List.of(senderId, receiverId));
    }

    @Override
    public void persistGroupMessage(MessageSendDTO messageSendDTO, MessageReceiveDTO messageReceiveDTO) {
        Message message = new Message();
        ObjectId groupId = new ObjectId(messageSendDTO.getReceiverName());

        String collectionName = buildCollectionName(groupId, null, ChatType.GROUP);
        User sender = userService.findByUsername(messageReceiveDTO.getSenderName());

        GroupChat groupChat = groupChatService.findById(groupId)
                .orElseThrow(()->new DocumentNotFoundException("Chat " + groupId.toHexString() + " not found"));

        message.setId(sender.getUserId().toHexString()+messageReceiveDTO.getTimestamp());
        message.setSenderId(sender.getUserId());
        message.setContent(messageReceiveDTO.getContent());
        message.setType(messageReceiveDTO.getType());
        message.setTimestamp(messageReceiveDTO.getTimestamp());
        saveMessage(collectionName, message);

        readStatusService.createReadStatus(collectionName, message, groupChat.getMemberIds());
    }

    @Override
    public void persistGroupFile(InputStream inputStream, MessageType messageType, ObjectId senderId, ObjectId groupId) {
        String collectionName = buildCollectionName(groupId, null, ChatType.GROUP);
        long timestamp = Instant.now().toEpochMilli();
        String fileId = senderId.toHexString()+timestamp;

        GroupChat groupChat = groupChatService.findById(groupId)
                .orElseThrow(()->new DocumentNotFoundException("Chat " + groupId.toHexString() + " not found"));

        //upload to s3
        String key = collectionName + "/" + fileId;
        PutObjectResult result = s3Service.uploadChatFile(inputStream, messageType, key);

        Message message = persistFileMessage(collectionName, senderId, fileId, messageType, timestamp);

        readStatusService.createReadStatus(collectionName, message, groupChat.getMemberIds());
    }

    @Override
    public void persistFile(InputStream inputStream, MessageType messageType, ObjectId senderId, ObjectId receiverId,
                             ChatType chatType){
        String collectionName = buildCollectionName(senderId, receiverId, chatType);
        long timestamp = Instant.now().toEpochMilli();
        String fileId = senderId.toHexString()+timestamp;

        //upload to s3
        String key = collectionName + "/" + fileId;
        PutObjectResult result = s3Service.uploadChatFile(inputStream, messageType, key);

        Message message = persistFileMessage(collectionName, senderId, fileId, messageType, timestamp);

        readStatusService.createReadStatus(collectionName, message, List.of(senderId, receiverId));
    }

    private Message persistFileMessage(String collectionName, ObjectId senderId, String fileId,
                                       MessageType messageType, long timestamp){
        Message message = new Message();
        message.setId(fileId);
        message.setSenderId(senderId);
        message.setContent("http://localhost:8080/api/v2/messages/files/" + timestamp);
        message.setType(messageType);
        message.setTimestamp(timestamp);
        return saveMessage(collectionName, message);
    }

    @Override
    public boolean collectionExists(String collectionName) {
        return messageRepository.collectionExists(collectionName);
    }

    @Override
    public MessagesDTO getAllMessages(MyUserDetails userDetails){
        User user = userService.findById(new ObjectId(userDetails.getUserId()));

        List<ObjectId> privateChats = user.getPrivateChats();
        HashMap<String, List<MessageDTO>> privateChatsHM = new HashMap<>();

        for (ObjectId privateChatId : privateChats) {
            User targetUser = userService.findById(privateChatId);
            privateChatsHM.put(targetUser.getUsername(), getPrivateChatMessages(userDetails, targetUser.getUsername()));
        }

        List<ObjectId> botChats = user.getBotChats();
        HashMap<String, List<MessageDTO>> botChatsHM = new HashMap<>();

        for (ObjectId botChatId : botChats) {
            Bot bot = botService.getBotById(botChatId);
            botChatsHM.put(bot.getName(), getBotChatMessages(userDetails, bot.getName()));
        }

        List<ObjectId> groupChats = user.getGroupChats();
        HashMap<String, List<MessageDTO>> groupChatsHM = new HashMap<>();

        for (ObjectId groupChatId : groupChats) {
            GroupChat groupChat = groupChatService.findById(groupChatId).orElseThrow();
            groupChatsHM.put(groupChat.getId().toHexString(), getGroupChatMessages(userDetails, groupChat));
        }

        return MessagesDTO.builder()
                .PRIVATE(privateChatsHM)
                .BOT(botChatsHM)
                .GROUP(groupChatsHM)
                .build();
    }

    @Override
    public List<MessageDTO> getPrivateChatMessages(MyUserDetails userDetails, String targetUserName){
        ObjectId targetUserId = userService.findByUsername(targetUserName).getUserId();
        String chatCollection = buildCollectionName(new ObjectId(userDetails.getUserId()), targetUserId, ChatType.PRIVATE);
        return getCollectionMessages(userDetails, targetUserName, chatCollection);
    }

    @Override
    public List<MessageDTO> getBotChatMessages(MyUserDetails userDetails, String botName){
        Bot bot = botService.getBotByName(botName);
        String chatCollection = buildCollectionName(new ObjectId(userDetails.getUserId()), bot.getId(), ChatType.BOT);
        return getCollectionMessages(userDetails, botName, chatCollection);
    }

    @Override
    public List<MessageDTO> getGroupChatMessages(MyUserDetails userDetails, GroupChat groupChat){
        List<MessageDTO> messageDTOS = new ArrayList<>();

        if(!groupChat.getMemberIds().contains(new ObjectId(userDetails.getUserId()))){
            return messageDTOS;
        }

        String chatCollection = buildCollectionName(groupChat.getId(), null, ChatType.GROUP);
        List<Message> messages = messageRepository.findAll(chatCollection);

        for (Message message : messages) {
            MessageDTO messageDTO = MessageDTO.builder()
                    .type(message.getType())
                    .content(message.getContent())
                    .senderName(userService.findById(message.getSenderId()).getUsername())
                    .timestamp(message.getTimestamp())
                    .build();
            messageDTOS.add(messageDTO);
        }
        return messageDTOS;
    }

    private List<MessageDTO> getCollectionMessages(MyUserDetails userDetails, String chatName, String collectionName){
        List<Message> messages = messageRepository.findAll(collectionName);
        List<MessageDTO> messageDTOS = new ArrayList<>();
        for (Message message : messages) {
            ReadStatus readStatus = readStatusService.getReadStatus(collectionName, message,
                    new ObjectId(userDetails.getUserId()));
            MessageDTO messageDTO = MessageDTO.builder()
                    .type(message.getType())
                    .content(message.getContent())
                    .timestamp(message.getTimestamp())
                    .isRead(readStatus.isRead())
                    .build();
            if(message.getSenderId().toHexString().equals(userDetails.getUserId())){
                messageDTO.setSenderName(userDetails.getUsername());
            }else{
                messageDTO.setSenderName(chatName);
            }
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

    @Override
    public S3File findFileById(MyUserDetails userDetails, String chatName,
                                String senderName, ChatType chatType, String fileId) {
        String key;
        ObjectId userId = new ObjectId(userDetails.getUserId());
        ObjectId senderId = new ObjectId();

        switch (chatType){
            case PRIVATE:
                ObjectId userId2 = userService.findByUsername(chatName).getUserId();
                if(userDetails.getUsername().equals(senderName)){
                    senderId = userId;
                }else{
                    senderId = userId2;
                }
                key = buildCollectionName(userId, userId2, chatType) + "/" + senderId.toHexString() + fileId;
                break;
            case BOT:
                    ObjectId botId = botService.getBotByName(chatName).getId();
                    if(userDetails.getUsername().equals(senderName)){
                        senderId = userId;
                    }else{
                        senderId = botId;
                    }
                    key = buildCollectionName(userId, botId, chatType) + "/" + senderId.toHexString() + fileId;
                break;
            case GROUP:
                        GroupChat groupChat = groupChatService.findById(new ObjectId(chatName)).orElseThrow();
                        List<ObjectId> memberIds = groupChat.getMemberIds();
                        for (ObjectId memberId : memberIds) {
                            User member = userService.findById(memberId);
                            if(member.getUsername().equals(senderName)){
                                senderId = memberId;
                            }
                        }
                        key = buildCollectionName(groupChat.getId(), null, chatType) + "/" + senderId.toHexString() + fileId;
                 break;
            default:
                key = "dummy";
                 break;
        }
        return s3Service.getChatFile(key);
    }

    private String buildCollectionName(ObjectId id1, ObjectId id2, ChatType chatType){
        String collectionName = chatType + "_";
        if(id2 == null){
            return collectionName + id1.toHexString();
        }
        if(id1.getTimestamp() < id2.getTimestamp()){
            collectionName += id1 + "&" + id2;
        }else if(id1.getTimestamp() > id2.getTimestamp()){
            collectionName += id2 + "&" + id1;
        }else{
            int result = id1.compareTo(id2);
            if(result < 0){
                collectionName += id1 + "&" + id2;
            }else if(result > 0){
                collectionName += id2 + "&" + id1;
            }else{
                collectionName += id1;
            }
        }
        return collectionName;
    }
}
