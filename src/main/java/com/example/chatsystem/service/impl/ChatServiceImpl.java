package com.example.chatsystem.service.impl;

import com.amazonaws.services.s3.model.PutObjectResult;
import com.example.chatsystem.bot.Bot;
import com.example.chatsystem.bot.BotService;
import com.example.chatsystem.dto.chat.*;
import com.example.chatsystem.dto.groupchat.CreateGroupRequest;
import com.example.chatsystem.exception.DocumentNotFoundException;
import com.example.chatsystem.model.*;
import com.example.chatsystem.repository.GroupChatRepo;
import com.example.chatsystem.service.*;
import com.fasterxml.uuid.Generators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.example.chatsystem.utils.CollectionUtils.buildCollectionName;

@Service
public class ChatServiceImpl implements ChatService {
    private final GroupChatRepo groupChatRepo;
    private final UserService userService;
    private final WebSocketService webSocketService;
    private final BotService botService;
    private final ReadStatusService readStatusService;
    private final S3Service s3Service;
    private final MessageService messageService;

    @Value("${aws.avatars.url}")
    private String avatarsUrl;

    @Autowired
    public ChatServiceImpl(GroupChatRepo groupChatRepo, UserService userService, WebSocketService webSocketService, BotService botService, ReadStatusService readStatusService, S3Service s3Service, MessageService messageService) {
        this.groupChatRepo = groupChatRepo;
        this.userService = userService;
        this.webSocketService = webSocketService;
        this.botService = botService;
        this.readStatusService = readStatusService;
        this.s3Service = s3Service;
        this.messageService = messageService;
    }

    @Override
    public GroupChat findById(UUID id) {
        return groupChatRepo.findById(id).orElseThrow(()->new DocumentNotFoundException("Chat " + id.toString() + " not found"));
    }

    @Override
    public GroupChatResponse findById(UUID userId, UUID groupId) {
        GroupChat groupChat = findById(groupId);
        User user = userService.findById(groupChat.getHostId());

        if(groupChat.getMemberIds().contains(userId)){
            return GroupChatResponse.builder()
                    .id(groupId)
                    .members(groupChat.getMemberIds())
                    .name(groupChat.getName())
                    .hostId(user.getId())
                    .image(groupChat.getImage())
                    .type(ChatType.GROUP)
                    .state(ChatState.NONE)
                    .build();
        }

        return null;
    }

    @Override
    public GroupChat updateGroupChat(GroupChat groupChat) {
        return groupChatRepo.save(groupChat);
    }

    @Override
    public GroupChat changeGroupChatHost(UUID userId, UUID chatId, String hostName) {
        GroupChat groupChat = findById(chatId);
        User newHostUser = userService.findByUsername(hostName);
        if(isHost(groupChat, userId)){
            groupChat.setHostId(newHostUser.getId());
        }
        return updateGroupChat(groupChat);
    }

    @Override
    public void deleteGroupChat(UUID userId, UUID groupId) {
        GroupChat groupChat = findById(groupId);
        if (isHost(groupChat, userId)) {
            List<UUID> memberIds = groupChat.getMemberIds();
            for (UUID memberId : memberIds) {
                webSocketService.unsubscribeUserFromGroup(memberId, groupId);
            }
            groupChatRepo.delete(groupChat);
        }
    }

    @Override
    public GroupChat addMemberToGroupChat(UUID userId, UUID chatId, String memberName) {
        GroupChat groupChat = findById(chatId);
        User member = userService.findByUsername(memberName);

        if(isHost(groupChat, userId)){
            addUserToGroup(member, groupChat);
        }
        return updateGroupChat(groupChat);
    }

    @Override
    public GroupChat removeMemberFromGroupChat(UUID userId, UUID chatId, String memberName) {
        GroupChat groupChat = findById(chatId);
        User member = userService.findByUsername(memberName);

        if(isHost(groupChat, userId)){
            removeUserFromGroup(member, groupChat);
        }
        return updateGroupChat(groupChat);
    }

    @Override
    public void addUserToGroup(UUID userId, UUID chatId){
        GroupChat groupChat = findById(chatId);
        User user = userService.findById(userId);
        addUserToGroup(user, groupChat);
    }

    @Override
    public void removeUserFromGroup(UUID userId, UUID chatId){
        GroupChat groupChat = findById(chatId);
        User user = userService.findById(userId);
        removeUserFromGroup(user, groupChat);
        String collectionName = buildCollectionName(chatId, null);
        messageService.updateLastMessageStatus(userId, collectionName);
    }

    private void addUserToGroup(User user, GroupChat groupChat) {
        groupChat.getMemberIds().add(user.getId());
        userService.addGroupChatToUser(user.getId(), groupChat.getId());
        webSocketService.subscribeUserToGroup(user.getId(), groupChat.getId());
    }

    private void removeUserFromGroup(User user, GroupChat groupChat){
        groupChat.getMemberIds().remove(user.getId());
        groupChatRepo.save(groupChat);

        userService.removeGroupChatFromUser(user.getId(), groupChat.getId());
        webSocketService.unsubscribeUserFromGroup(user.getId(), groupChat.getId());
    }

    @Override
    public GroupChat changeGroupChatName(UUID userId, UUID chatId, String newName) {
        GroupChat groupChat = findById(chatId);
        if(isHost(groupChat, userId)) {
            groupChat.setName(newName);
        }
        return updateGroupChat(groupChat);
    }

    @Override
    public GroupChatResponse createGroupChat(UUID userId, CreateGroupRequest request) {
        GroupChat groupChat = GroupChat.builder()
                .name(request.getName())
                .hostId(userId)
                .build();

        User hostUser = userService.findById(userId);

        List<UUID> memberIds = new ArrayList<>(request.getMemberIds());
        
        memberIds.add(userId);
        groupChat.setMemberIds(memberIds);

        UUID groupId = Generators.defaultTimeBasedGenerator().generate();

        groupChat.setId(groupId);

        //  save image and update image link
        MultipartFile image = request.getImage();
        try {
            if(image.getSize() != 0){
                PutObjectResult result = s3Service.uploadAvatar(image.getInputStream(), groupId.toString(), image.getContentType());
                groupChat.setImage(avatarsUrl + groupId.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //  save to mongodb
        GroupChat createdGroupChat = groupChatRepo.save(groupChat);

        //  add group chat to every member
        for (UUID memberId : memberIds) {
            userService.addGroupChatToUser(memberId, createdGroupChat.getId());
            webSocketService.subscribeUserToGroup(memberId, createdGroupChat.getId());
        }

        return GroupChatResponse.builder()
                .id(createdGroupChat.getId())
                .members(createdGroupChat.getMemberIds())
                .name(createdGroupChat.getName())
                .hostId(hostUser.getId())
                .image(createdGroupChat.getImage())
                .type(ChatType.GROUP)
                .state(ChatState.NONE)
                .build();
    }

    @Override
    public ChatResponseDTO findAllChats(UUID userId) {
        User user = userService.findById(userId);

        ArrayList<PrivateChatResponse> privateChatDTOs = new ArrayList<>();
        setUserPrivateChats(privateChatDTOs, user);

        ArrayList<GroupChatResponse> groupChatDTOs = new ArrayList<>();
        setUserGroupChats(groupChatDTOs, user);

        ArrayList<BotChatResponse> botChatDTOs = new ArrayList<>();
        setUserBotChats(botChatDTOs, user);

        return ChatResponseDTO.builder()
                .PRIVATE(privateChatDTOs)
                .GROUP(groupChatDTOs)
                .BOT(botChatDTOs)
                .build();
    }

    @Override
    public void deleteChats(UUID userId, DeleteChatsRequest deleteChatsRequest) {
        for(UUID privateChatId: deleteChatsRequest.getPrivateChats()){
            deletePrivateChat(userId, privateChatId, deleteChatsRequest.isBoth());
        }

        for (UUID groupChatId: deleteChatsRequest.getGroupChats()) {
            removeUserFromGroup(userId, groupChatId);
        }

        for (UUID botChatId: deleteChatsRequest.getBotChats()) {
            deleteBotChat(userId, botChatId);
        }
    }

    @Override
    public ArrayList<PrivateChatResponse> findPrivateChats(UUID userId) {
        ArrayList<PrivateChatResponse> chatDTOs = new ArrayList<>();
        User user;
        try {
            user = userService.findById(userId);
        }catch (DocumentNotFoundException e){
            user = User.builder()
                    .username("unknown")
                    .build();
        }

        setUserPrivateChats(chatDTOs, user);
        return chatDTOs;
    }

    @Override
    public PrivateChatResponse findPrivateChatById(UUID userId, UUID targetUserId) {
        User targetUser = userService.findById(targetUserId);
        String collectionName = buildCollectionName(userId, targetUserId);
        ReadStatus readStatus = readStatusService.getReadStatus(collectionName, userId);
        return PrivateChatResponse.builder()
                .username(targetUser.getUsername())
                .avatar(targetUser.getAvatar())
                .lastReadTime(readStatus.getLastReadTime())
                .type(ChatType.PRIVATE)
                .state(ChatState.NONE)
                .build();
    }

    @Override
    public void addPrivateChat(UUID userId, AddChatRequest privateChatDTO) {
        try {
            userService.findById(userId);
            userService.addPrivateChatToUser(userId, privateChatDTO.getChatId());
        }catch (DocumentNotFoundException e){
            throw new DocumentNotFoundException(e.getMessage());
        }

    }

    @Override
    public void deletePrivateChat(UUID userId, UUID targetUserId, boolean isBoth) {
        userService.removePrivateChatFromUser(userId, targetUserId);
        String collectionName = buildCollectionName(userId, targetUserId);
        messageService.updateLastMessageStatus(userId, collectionName);

        if(isBoth){
            userService.removePrivateChatFromUser(targetUserId, userId);
            messageService.updateLastMessageStatus(targetUserId, collectionName);
        }
    }

    @Override
    public ArrayList<GroupChatResponse> findGroupChats(UUID userId) {
        ArrayList<GroupChatResponse> chatDTOs = new ArrayList<>();
        User user = userService.findById(userId);
        setUserGroupChats(chatDTOs, user);
        return chatDTOs;
    }

    @Override
    public ArrayList<BotChatResponse> findBotChats(UUID userId){
        ArrayList<BotChatResponse> botChatDTOs = new ArrayList<>();
        User user = userService.findById(userId);
        setUserBotChats(botChatDTOs, user);
        return botChatDTOs;
    }

    @Override
    public void deleteBotChat(UUID userId, UUID botId) {
        userService.removeBotChatFromUser(userId, botId);
        String collectionName = buildCollectionName(userId, botId);
        messageService.updateLastMessageStatus(userId, collectionName);
    }

    @Override
    public ArrayList<String> findGroupChatMemberNames(UUID chatId){
        ArrayList<String> memberNames = new ArrayList<>();
        GroupChat groupChat = findById(chatId);
        groupChat.getMemberIds().forEach(memberId -> {
            User user = userService.findById(memberId);
            memberNames.add(user.getUsername());
        });
        return memberNames;
    }

    private boolean isHost(GroupChat groupChat, UUID userId){
        return groupChat.getHostId().equals(userId);
    }

    private void setUserPrivateChats(ArrayList<PrivateChatResponse> chatDTOs, User user) {
        List<UUID> chatUserIds = user.getPrivateChats();

        for (UUID chatUserId : chatUserIds) {
            String collectionName = buildCollectionName(user.getId(), chatUserId);
            ReadStatus readStatus = readStatusService.getReadStatus(collectionName, user.getId());

            User receiver = userService.findById(chatUserId);
            chatDTOs.add(PrivateChatResponse.builder()
                    .username(receiver.getUsername())
                    .avatar(receiver.getAvatar())
                    .type(ChatType.PRIVATE)
                    .lastReadTime(readStatus.getLastReadTime())
                    .state(ChatState.NONE)
                    .id(chatUserId)
                    .build());
        }
    }

    private void setUserGroupChats(ArrayList<GroupChatResponse> chatDTOs, User user) {
        List<UUID> groupChatIds = user.getGroupChats();
        for (UUID chatId : groupChatIds) {
            String collectionName = buildCollectionName(chatId, null);
            ReadStatus readStatus = readStatusService.getReadStatus(collectionName, user.getId());

            GroupChat groupChat = findById(chatId);
            User hostUser = userService.findById(groupChat.getHostId());
            chatDTOs.add(GroupChatResponse.builder()
                    .id(chatId)
                    .members(groupChat.getMemberIds())
                    .name(groupChat.getName())
                    .hostId(hostUser.getId())
                    .image(groupChat.getImage())
                    .type(ChatType.GROUP)
                    .lastReadTime(readStatus.getLastReadTime())
                    .state(ChatState.NONE)
                    .build());
        }
    }

    private void setUserBotChats(ArrayList<BotChatResponse> chatDTOs, User user) {
        List<UUID> botIds = user.getBotChats();
        for (UUID botId : botIds) {
            String collectionName = buildCollectionName(user.getId(), botId);
            ReadStatus readStatus = readStatusService.getReadStatus(collectionName, user.getId());

            Bot bot = botService.getBotById(botId);
            chatDTOs.add(BotChatResponse.builder()
                            .botName(bot.getName())
                            .type(ChatType.BOT)
                            .avatar(bot.getAvatar())
                            .lastReadTime(readStatus.getLastReadTime())
                            .id(botId)
                    .state(ChatState.ONLINE)
                    .build());
        }
    }
}
