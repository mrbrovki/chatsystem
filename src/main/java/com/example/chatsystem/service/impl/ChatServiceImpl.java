package com.example.chatsystem.service.impl;

import com.example.chatsystem.bot.Bot;
import com.example.chatsystem.bot.BotService;
import com.example.chatsystem.dto.chat.*;
import com.example.chatsystem.exception.DocumentNotFoundException;
import com.example.chatsystem.model.ChatType;
import com.example.chatsystem.model.GroupChat;
import com.example.chatsystem.model.User;
import com.example.chatsystem.service.ChatService;
import com.example.chatsystem.service.GroupChatService;
import com.example.chatsystem.service.UserService;
import com.example.chatsystem.service.WebSocketService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {
    private final GroupChatService groupChatService;
    private final UserService userService;
    private final WebSocketService webSocketService;
    private final BotService botService;

    @Value("${aws.avatars.url}")
    private String avatarsUrl;

    @Autowired
    public ChatServiceImpl(GroupChatService groupChatService, UserService userService, WebSocketService webSocketService, BotService botService) {
        this.groupChatService = groupChatService;
        this.userService = userService;
        this.webSocketService = webSocketService;
        this.botService = botService;
    }

    @Override
    public GroupChat findById(ObjectId id) {
        return groupChatService.findById(id).orElseThrow(()->new DocumentNotFoundException("Chat " + id.toHexString() + " not found"));
    }

    @Override
    public GroupChatResponseDTO findById(ObjectId userId, ObjectId groupId) {
        GroupChat groupChat = findById(groupId);
        User user = userService.findById(groupChat.getHostId());

        if(groupChat.getMemberIds().contains(userId)){
            return GroupChatResponseDTO.builder()
                    .id(groupId.toHexString())
                    .members(groupChat.getMemberIds().stream().map(ObjectId::toHexString).collect(Collectors.toList()))
                    .name(groupChat.getName())
                    .host(user.getUsername())
                    .image(avatarsUrl + groupId.toHexString())
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
    public GroupChatResponseDTO createGroupChat(ObjectId userId, GroupChatCreateDTO groupChatCreateDTO) {
        GroupChat groupChat = GroupChat.builder()
                .name(groupChatCreateDTO.getName())
                .hostId(userId)
                .build();

        User hostUser = userService.findById(userId);

        List<ObjectId> memberIds = new ArrayList<>();

        for (String memberName : groupChatCreateDTO.getMemberNames()) {
            memberIds.add(userService.findByUsername(memberName).getUserId());
        }

        memberIds.add(userId);

        groupChat.setMemberIds(memberIds);

        GroupChat createdGroupchat = groupChatService.save(groupChat);

        for (ObjectId memberId : memberIds) {
            User user = userService.findById(memberId);
            userService.addGroupChatToUser(user, createdGroupchat.getId());
            webSocketService.subscribeUserToGroup(user.getUsername(), createdGroupchat.getId());
        }

        return GroupChatResponseDTO.builder()
                .id(createdGroupchat.getId().toHexString())
                .members(createdGroupchat.getMemberIds().stream().map(ObjectId::toHexString).collect(Collectors.toList()))
                .name(createdGroupchat.getName())
                .host(hostUser.getUsername())
                .image(avatarsUrl + createdGroupchat.getId().toHexString())
                .type(ChatType.GROUP)
                .build();
    }

    @Override
    public ChatResponseDTO findAllChats(ObjectId userId) {
        User user = userService.findById(userId);

        ArrayList<PrivateChatResponseDTO> privateChatDTOs = new ArrayList<>();
        setUserPrivateChats(privateChatDTOs, user);

        ArrayList<GroupChatResponseDTO> groupChatDTOs = new ArrayList<>();
        setUserGroupChats(groupChatDTOs, user);

        ArrayList<BotChatResponseDTO> botChatDTOs = new ArrayList<>();
        setUserBotChats(botChatDTOs, user);

        return ChatResponseDTO.builder()
                .PRIVATE(privateChatDTOs)
                .GROUP(groupChatDTOs)
                .BOT(botChatDTOs)
                .build();
    }

    @Override
    public ArrayList<PrivateChatResponseDTO> findPrivateChats(ObjectId userId) {
        ArrayList<PrivateChatResponseDTO> chatDTOs = new ArrayList<>();
        User user = userService.findById(userId);
        setUserPrivateChats(chatDTOs, user);
        return chatDTOs;
    }

    @Override
    public PrivateChatResponseDTO findPrivateChatByName(String username) {
        User targetUser = userService.findByUsername(username);
        return PrivateChatResponseDTO.builder()
                .username(targetUser.getUsername())
                .avatar(avatarsUrl + targetUser.getUsername())
                .type(ChatType.PRIVATE)
                .build();
    }

    @Override
    public ArrayList<GroupChatResponseDTO> findGroupChats(ObjectId userId) {
        ArrayList<GroupChatResponseDTO> chatDTOs = new ArrayList<>();
        User user = userService.findById(userId);
        setUserGroupChats(chatDTOs, user);
        return chatDTOs;
    }

    @Override
    public ArrayList<BotChatResponseDTO> findBotChats(ObjectId userId){
        ArrayList<BotChatResponseDTO> botChatDTOs = new ArrayList<>();
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

    private void setUserPrivateChats(ArrayList<PrivateChatResponseDTO> chatDTOs, User user) {
        List<ObjectId> chatUserIds = user.getPrivateChats();

        for (ObjectId chatUserId : chatUserIds) {
            User receiver = userService.findById(chatUserId);
            chatDTOs.add(PrivateChatResponseDTO.builder()
                    .username(receiver.getUsername())
                    .avatar(avatarsUrl + receiver.getUsername())
                    .type(ChatType.PRIVATE)
                    .build());

        }
    }

    private void setUserGroupChats(ArrayList<GroupChatResponseDTO> chatDTOs, User user) {
        List<ObjectId> groupChatIds = user.getGroupChats();
        for (ObjectId chatId : groupChatIds) {
            GroupChat groupChat = findById(chatId);
            User hostUser = userService.findById(groupChat.getHostId());
            chatDTOs.add(GroupChatResponseDTO.builder()
                    .id(chatId.toHexString())
                    .members(groupChat.getMemberIds().stream().map(ObjectId::toHexString).collect(Collectors.toList()))
                    .name(groupChat.getName())
                    .host(hostUser.getUsername())
                    .image(avatarsUrl + chatId.toHexString())
                    .type(ChatType.GROUP)
                    .build());
        }
    }

    private void setUserBotChats(ArrayList<BotChatResponseDTO> chatDTOs, User user) {
        List<ObjectId> botIds = user.getBotChats();
        for (ObjectId botId : botIds) {
            Bot bot = botService.getBotById(botId);
            chatDTOs.add(BotChatResponseDTO.builder()
                            .botName(bot.getName())
                            .type(ChatType.BOT)
                            .avatar(avatarsUrl + bot.getName())
                    .build());
        }
    }
}
