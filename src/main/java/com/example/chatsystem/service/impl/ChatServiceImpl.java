package com.example.chatsystem.service.impl;

import com.amazonaws.services.s3.model.PutObjectResult;
import com.example.chatsystem.bot.Bot;
import com.example.chatsystem.bot.BotService;
import com.example.chatsystem.dto.chat.*;
import com.example.chatsystem.dto.groupchat.CreateGroupRequest;
import com.example.chatsystem.exception.DocumentNotFoundException;
import com.example.chatsystem.model.ChatType;
import com.example.chatsystem.model.GroupChat;
import com.example.chatsystem.model.ReadStatus;
import com.example.chatsystem.model.User;
import com.example.chatsystem.service.*;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.chatsystem.utils.CollectionUtils.buildCollectionName;

@Service
public class ChatServiceImpl implements ChatService {
    private final GroupChatService groupChatService;
    private final UserService userService;
    private final WebSocketService webSocketService;
    private final BotService botService;
    private final ReadStatusService readStatusService;
    private final S3Service s3Service;

    @Value("${aws.avatars.url}")
    private String avatarsUrl;

    @Autowired
    public ChatServiceImpl(GroupChatService groupChatService, UserService userService, WebSocketService webSocketService, BotService botService, ReadStatusService readStatusService, S3Service s3Service) {
        this.groupChatService = groupChatService;
        this.userService = userService;
        this.webSocketService = webSocketService;
        this.botService = botService;
        this.readStatusService = readStatusService;
        this.s3Service = s3Service;
    }

    @Override
    public GroupChat findById(ObjectId id) {
        return groupChatService.findById(id).orElseThrow(()->new DocumentNotFoundException("Chat " + id.toHexString() + " not found"));
    }

    @Override
    public GroupChatResponse findById(ObjectId userId, ObjectId groupId) {
        GroupChat groupChat = findById(groupId);
        User user = userService.findById(groupChat.getHostId());

        if(groupChat.getMemberIds().contains(userId)){
            return GroupChatResponse.builder()
                    .id(groupId.toHexString())
                    .members(groupChat.getMemberIds().stream().map(ObjectId::toHexString).collect(Collectors.toList()))
                    .name(groupChat.getName())
                    .host(user.getUsername())
                    .image(groupChat.getImage())
                    .type(ChatType.GROUP)
                    .build();
        }

        return null;
    }

    @Override
    public GroupChat updateGroupChat(GroupChat groupChat) {
        return groupChatService.save(groupChat);
    }

    @Override
    public GroupChat changeGroupChatHost(ObjectId userId, ObjectId chatId, String hostName) {
        GroupChat groupChat = findById(chatId);
        User newHostUser = userService.findByUsername(hostName);
        if(isHost(groupChat, userId)){
            groupChat.setHostId(newHostUser.getUserId());
        }
        return updateGroupChat(groupChat);
    }

    @Override
    public void deleteGroupChat(ObjectId userId, ObjectId chatId) {
        GroupChat groupChat = findById(chatId);
        if (isHost(groupChat, userId)) {
            List<ObjectId> memberIds = groupChat.getMemberIds();
            for (ObjectId memberId : memberIds) {
                webSocketService.unsubscribeUserToGroup(userService.findById(memberId).getUsername(), chatId);
            }
            groupChatService.delete(groupChat);
        }
    }

    @Override
    public GroupChat addMemberToGroupChat(ObjectId userId, ObjectId chatId, String memberName) {
        GroupChat groupChat = findById(chatId);
        User member = userService.findByUsername(memberName);

        if(isHost(groupChat, userId)){
            addUserToGroup(member, groupChat);
        }
        return updateGroupChat(groupChat);
    }

    @Override
    public GroupChat removeMemberFromGroupChat(ObjectId userId, ObjectId chatId, String memberName) {
        GroupChat groupChat = findById(chatId);
        User member = userService.findByUsername(memberName);

        if(isHost(groupChat, userId)){
            removeUserFromGroup(member, groupChat);
        }
        return updateGroupChat(groupChat);
    }

    @Override
    public void addUserToGroup(ObjectId userId, ObjectId chatId){
        GroupChat groupChat = findById(chatId);
        User user = userService.findById(userId);
        addUserToGroup(user, groupChat);
    }

    @Override
    public void removeUserFromGroup(ObjectId userId, ObjectId chatId){
        GroupChat groupChat = findById(chatId);
        User user = userService.findById(userId);
        removeUserFromGroup(user, groupChat);
    }

    private void addUserToGroup(User user, GroupChat groupChat) {
        groupChat.getMemberIds().add(user.getUserId());
        userService.addGroupChatToUser(user, groupChat.getId());
        webSocketService.subscribeUserToGroup(user.getUsername(), groupChat.getId());
    }

    private void removeUserFromGroup(User user, GroupChat groupChat){
        groupChat.getMemberIds().remove(user.getUserId());
        userService.removeGroupChatFromUser(user, groupChat.getId());
        webSocketService.unsubscribeUserToGroup(user.getUsername(), groupChat.getId());
    }

    @Override
    public GroupChat changeGroupChatName(ObjectId userId, ObjectId chatId, String newName) {
        GroupChat groupChat = findById(chatId);
        if(isHost(groupChat, userId)) {
            groupChat.setName(newName);
        }
        return updateGroupChat(groupChat);
    }

    @Override
    public GroupChatResponse createGroupChat(ObjectId userId, CreateGroupRequest request) {
        GroupChat groupChat = GroupChat.builder()
                .name(request.getName())
                .hostId(userId)
                .build();

        User hostUser = userService.findById(userId);

        List<ObjectId> memberIds = new ArrayList<>();

        for (String memberName : request.getMemberNames()) {
            memberIds.add(userService.findByUsername(memberName).getUserId());
        }
        memberIds.add(userId);
        groupChat.setMemberIds(memberIds);

        ObjectId groupId = new ObjectId();

        groupChat.setId(groupId);

        //  save image and update image link
        MultipartFile image = request.getImage();
        try {
            if(image.getSize() != 0){
                PutObjectResult result = s3Service.uploadAvatar(image.getInputStream(), groupId.toHexString(), image.getContentType());
                groupChat.setImage(avatarsUrl + groupId.toHexString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //  save to mongodb
        GroupChat createdGroupChat = groupChatService.save(groupChat);

        //  add group chat to every member
        for (ObjectId memberId : memberIds) {
            User user = userService.findById(memberId);
            userService.addGroupChatToUser(user, createdGroupChat.getId());
            webSocketService.subscribeUserToGroup(user.getUsername(), createdGroupChat.getId());
        }

        return GroupChatResponse.builder()
                .id(createdGroupChat.getId().toHexString())
                .members(createdGroupChat.getMemberIds().stream().map(ObjectId::toHexString).collect(Collectors.toList()))
                .name(createdGroupChat.getName())
                .host(hostUser.getUsername())
                .image(createdGroupChat.getImage())
                .type(ChatType.GROUP)
                .build();
    }

    @Override
    public ChatResponseDTO findAllChats(ObjectId userId) {
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
    public ArrayList<PrivateChatResponse> findPrivateChats(ObjectId userId) {
        ArrayList<PrivateChatResponse> chatDTOs = new ArrayList<>();
        User user = userService.findById(userId);
        setUserPrivateChats(chatDTOs, user);
        return chatDTOs;
    }

    @Override
    public PrivateChatResponse findPrivateChatByName(ObjectId userId, String targetName) {
        User targetUser = userService.findByUsername(targetName);
        String collectionName = buildCollectionName(userId, targetUser.getUserId(), ChatType.PRIVATE);
        ReadStatus readStatus = readStatusService.getReadStatus(collectionName, userId);
        return PrivateChatResponse.builder()
                .username(targetUser.getUsername())
                .avatar(targetUser.getAvatar())
                .lastReadTime(readStatus.getLastReadTime())
                .type(ChatType.PRIVATE)
                .build();
    }

    @Override
    public ArrayList<GroupChatResponse> findGroupChats(ObjectId userId) {
        ArrayList<GroupChatResponse> chatDTOs = new ArrayList<>();
        User user = userService.findById(userId);
        setUserGroupChats(chatDTOs, user);
        return chatDTOs;
    }

    @Override
    public ArrayList<BotChatResponse> findBotChats(ObjectId userId){
        ArrayList<BotChatResponse> botChatDTOs = new ArrayList<>();
        User user = userService.findById(userId);
        setUserBotChats(botChatDTOs, user);
        return botChatDTOs;
    }

    @Override
    public ArrayList<String> findGroupChatMemberNames(ObjectId chatId){
        ArrayList<String> memberNames = new ArrayList<>();
        GroupChat groupChat = findById(chatId);
        groupChat.getMemberIds().forEach(memberId -> {
            User user = userService.findById(memberId);
            memberNames.add(user.getUsername());
        });
        return memberNames;
    }

    private boolean isHost(GroupChat groupChat, ObjectId userId){
        return groupChat.getHostId().equals(userId);
    }

    private void setUserPrivateChats(ArrayList<PrivateChatResponse> chatDTOs, User user) {
        List<ObjectId> chatUserIds = user.getPrivateChats();

        for (ObjectId chatUserId : chatUserIds) {
            String collectionName = buildCollectionName(user.getUserId(), chatUserId, ChatType.PRIVATE);
            ReadStatus readStatus = readStatusService.getReadStatus(collectionName, user.getUserId());

            User receiver = userService.findById(chatUserId);
            chatDTOs.add(PrivateChatResponse.builder()
                    .username(receiver.getUsername())
                    .avatar(receiver.getAvatar())
                    .type(ChatType.PRIVATE)
                    .lastReadTime(readStatus.getLastReadTime())
                    .build());
        }
    }

    private void setUserGroupChats(ArrayList<GroupChatResponse> chatDTOs, User user) {
        List<ObjectId> groupChatIds = user.getGroupChats();
        for (ObjectId chatId : groupChatIds) {
            String collectionName = buildCollectionName(chatId, null, ChatType.GROUP);
            ReadStatus readStatus = readStatusService.getReadStatus(collectionName, user.getUserId());

            GroupChat groupChat = findById(chatId);
            User hostUser = userService.findById(groupChat.getHostId());
            chatDTOs.add(GroupChatResponse.builder()
                    .id(chatId.toHexString())
                    .members(groupChat.getMemberIds().stream().map(ObjectId::toHexString).collect(Collectors.toList()))
                    .name(groupChat.getName())
                    .host(hostUser.getUsername())
                    .image(groupChat.getImage())
                    .type(ChatType.GROUP)
                    .lastReadTime(readStatus.getLastReadTime())
                    .build());
        }
    }

    private void setUserBotChats(ArrayList<BotChatResponse> chatDTOs, User user) {
        List<ObjectId> botIds = user.getBotChats();
        for (ObjectId botId : botIds) {
            String collectionName = buildCollectionName(user.getUserId(), botId, ChatType.BOT);
            ReadStatus readStatus = readStatusService.getReadStatus(collectionName, user.getUserId());

            Bot bot = botService.getBotById(botId);
            chatDTOs.add(BotChatResponse.builder()
                            .botName(bot.getName())
                            .type(ChatType.BOT)
                            .avatar(bot.getAvatar())
                            .lastReadTime(readStatus.getLastReadTime())
                    .build());
        }
    }
}
