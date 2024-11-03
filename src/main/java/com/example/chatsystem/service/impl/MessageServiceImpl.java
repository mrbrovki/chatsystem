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
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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
    public void persistPrivateMessage(MessageReceiveDTO messageReceiveDTO, ObjectId senderId, String receiverName) {
        User receiver = userService.findByUsername(receiverName);
        String collectionName = buildCollectionName(senderId, receiver.getUserId(), ChatType.PRIVATE);

        if(!collectionExists(collectionName)) {
            userService.addPrivateChatToUser(senderId, receiver.getUserId());
        }
        userService.addPrivateChatToUser(receiver.getUserId(), senderId);

        Message message = persistMessage(collectionName, senderId, messageReceiveDTO);
        readStatusService.persist(senderId, collectionName);
    }

    @Override
    public void persistPrivateMessage(MessageReceiveDTO messageReceiveDTO, String collectionName,
                                      ObjectId senderId, ObjectId receiverId) {
        if(!collectionExists(collectionName)) {
            userService.addPrivateChatToUser(senderId, receiverId);
            userService.addPrivateChatToUser(receiverId, senderId);
        }

        Message message = persistMessage(collectionName, senderId, messageReceiveDTO);
        readStatusService.persist(senderId, collectionName);
    }

    @Override
    public void persistBotMessage(MessageReceiveDTO messageReceiveDTO, ObjectId senderId, ObjectId receiverId) {
        String collectionName = buildCollectionName(senderId, receiverId, ChatType.BOT);

        Message message =  persistMessage(collectionName, senderId, messageReceiveDTO);
        readStatusService.persist(senderId, collectionName);
    }

    @Override
    public void persistBotMessage(MessageReceiveDTO messageReceiveDTO, String collectionName,
                                  ObjectId senderId, ObjectId receiverId) {
        Message message = persistMessage(collectionName, senderId, messageReceiveDTO);
        readStatusService.persist(senderId, collectionName);
    }

    @Override
    public void persistGroupMessage(MessageReceiveDTO messageReceiveDTO, ObjectId senderId, ObjectId groupId) {
        String collectionName = buildCollectionName(groupId, null, ChatType.GROUP);

        Message message = persistMessage(collectionName, senderId, messageReceiveDTO);
        readStatusService.persist(senderId, collectionName);
    }

    @Override
    public void persistGroupMessage(MessageReceiveDTO messageReceiveDTO, String collectionName,
                                    ObjectId senderId) {
        Message message = persistMessage(collectionName, senderId, messageReceiveDTO);
        readStatusService.persist(senderId, collectionName);
    }

    @Override
    public void persistGroupFile(MessageReceiveDTO messageReceiveDTO, InputStream inputStream, ObjectId senderId, ObjectId groupId) {
        String collectionName = buildCollectionName(groupId, null, ChatType.GROUP);

        //  upload to s3
        String fileId = senderId.toHexString()+messageReceiveDTO.getTimestamp();
        String key = collectionName + "/" + fileId;
        PutObjectResult result = s3Service.uploadChatFile(inputStream, messageReceiveDTO.getType(), key);

        //  persist
        persistGroupMessage(messageReceiveDTO, collectionName, senderId);
    }

    @Override
    public void persistBotFile(MessageReceiveDTO messageReceiveDTO, InputStream inputStream, ObjectId senderId, ObjectId receiverId){
        String collectionName = buildCollectionName(senderId, receiverId, ChatType.BOT);

        //upload to s3
        String fileId = senderId.toHexString()+messageReceiveDTO.getTimestamp();
        String key = collectionName + "/" + fileId;
        PutObjectResult result = s3Service.uploadChatFile(inputStream, messageReceiveDTO.getType(), key);

        //  persist
        persistBotMessage(messageReceiveDTO, collectionName, senderId, receiverId);
    }

    @Override
    public void persistPrivateFile(MessageReceiveDTO messageReceiveDTO, InputStream inputStream,
                                   ObjectId senderId, String receiverName){
        User receiver = userService.findByUsername(receiverName);
        String collectionName = buildCollectionName(senderId, receiver.getUserId(), ChatType.PRIVATE);

        //upload to s3
        String fileId = senderId.toHexString()+messageReceiveDTO.getTimestamp();
        String key = collectionName + "/" + fileId;
        PutObjectResult result = s3Service.uploadChatFile(inputStream, messageReceiveDTO.getType(), key);

        //  persist
        persistPrivateMessage(messageReceiveDTO, collectionName, senderId, receiver.getUserId());
    }

    private Message persistMessage(String collectionName, ObjectId senderId, MessageReceiveDTO messageReceiveDTO) {
        Message message = new Message();
        message.setId(senderId.toHexString()+messageReceiveDTO.getTimestamp());
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
        User user = userService.findById(new ObjectId(userDetails.getUserId()));

        List<ObjectId> privateChats = user.getPrivateChats();
        HashMap<String, List<MessageDTO>> privateChatsHM = new HashMap<>();

        for (ObjectId privateChatId : privateChats) {
            User targetUser = userService.findById(privateChatId);
            privateChatsHM.put(targetUser.getUsername(), getPrivateChatMessages(new ObjectId(userDetails.getUserId()), userDetails.getUsername(), targetUser.getUsername()));
        }

        List<ObjectId> botChats = user.getBotChats();
        HashMap<String, List<MessageDTO>> botChatsHM = new HashMap<>();

        for (ObjectId botChatId : botChats) {
            Bot bot = botService.getBotById(botChatId);
            botChatsHM.put(bot.getName(), getBotChatMessages(new ObjectId(userDetails.getUserId()), userDetails.getUsername(),  bot.getName()));
        }

        List<ObjectId> groupChats = user.getGroupChats();
        HashMap<String, List<MessageDTO>> groupChatsHM = new HashMap<>();

        for (ObjectId groupChatId : groupChats) {
            GroupChat groupChat = groupChatRepo.findById(groupChatId).orElseThrow(()->
                    new DocumentNotFoundException("Group " + groupChatId.toHexString() + " not found"));
            groupChatsHM.put(groupChat.getId().toHexString(), getGroupChatMessages(new ObjectId(userDetails.getUserId()), groupChat));
        }

        return MessagesResponse.builder()
                .PRIVATE(privateChatsHM)
                .BOT(botChatsHM)
                .GROUP(groupChatsHM)
                .build();
    }

    @Override
    public List<MessageDTO> getPrivateChatMessages(ObjectId userId, String username, String targetUserName){
        ObjectId targetUserId = userService.findByUsername(targetUserName).getUserId();
        String chatCollection = buildCollectionName(userId, targetUserId, ChatType.PRIVATE);
        return getCollectionMessages(userId, username, targetUserName, chatCollection);
    }

    @Override
    public List<MessageDTO> getBotChatMessages(ObjectId userId, String username, String botName){
        Bot bot = botService.getBotByName(botName);
        String chatCollection = buildCollectionName(userId, bot.getId(), ChatType.BOT);
        return getCollectionMessages(userId, username, botName, chatCollection);
    }

    @Override
    public List<MessageDTO> getGroupChatMessages(ObjectId userId, GroupChat groupChat){
        List<MessageDTO> messageDTOS = new ArrayList<>();

        if(!groupChat.getMemberIds().contains(userId)){
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

    private List<MessageDTO> getCollectionMessages(ObjectId userId, String username, String chatName, String collectionName){
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
            if(message.getSenderId().toHexString().equals(userId.toHexString())){
                messageDTO.setSenderName(username);
            }else{
                messageDTO.setSenderName(chatName);
            }
            messageDTOS.add(messageDTO);
        }
        return messageDTOS;
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
                        GroupChat groupChat = groupChatRepo.findById(new ObjectId(chatName))
                                .orElseThrow(()->
                                        new DocumentNotFoundException("Group " + chatName + " not found"));
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


    @Override
    public void updateLastMessageStatus(ObjectId userId, ObjectId targetUserId) {
        String collectionName = buildCollectionName(userId, targetUserId, ChatType.PRIVATE);
        Optional<Message> lastMessage = messageRepository.findLastMessage(collectionName);
        lastMessage.ifPresent(message -> readStatusService.updateLastMessage(collectionName, message.getId(), userId));
    }

    @Override
    public void updatePrivateReadStatus(ObjectId userId, String targetUsername) {
        User targetUser = userService.findByUsername(targetUsername);
        String collectionName = buildCollectionName(userId, targetUser.getUserId(), ChatType.PRIVATE);
        readStatusService.updateTimeRead(collectionName, userId);
    }

    @Override
    public void updateGroupReadStatus(ObjectId userId, ObjectId groupId) {
        String collectionName = buildCollectionName(groupId, null, ChatType.GROUP);
        readStatusService.updateTimeRead(collectionName, userId);
    }

    @Override
    public void updateBotReadStatus(ObjectId userId, String botName) {
        Bot bot = botService.getBotByName(botName);
        String collectionName = buildCollectionName(userId, bot.getId(), ChatType.BOT);
        readStatusService.updateTimeRead(collectionName, userId);
    }
}
