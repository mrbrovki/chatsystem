package com.example.chatsystem.service;

import com.example.chatsystem.dto.chat.*;
import com.example.chatsystem.dto.groupchat.CreateGroupRequest;
import com.example.chatsystem.model.GroupChat;

import java.util.ArrayList;
import java.util.UUID;

public interface ChatService {
    //  groupchat
    ArrayList<GroupChatResponse> findGroupChats(UUID userId);
    GroupChat findById(UUID groupId);
    GroupChatResponse findById(UUID userId, UUID groupId);
    GroupChat updateGroupChat(GroupChat groupChat);
    GroupChat changeGroupChatHost(UUID userId, UUID groupId, String hostName);
    void deleteGroupChat(UUID userId, UUID groupId);
    GroupChat addMemberToGroupChat(UUID userId, UUID groupId, String memberName);
    GroupChat removeMemberFromGroupChat(UUID userId, UUID groupId, String memberName);
    void addUserToGroup(UUID userId, UUID groupId);
    void removeUserFromGroup(UUID userId, UUID groupId);
    GroupChat changeGroupChatName(UUID userId, UUID groupId, String newName);
    GroupChatResponse createGroupChat(UUID userId, CreateGroupRequest request);
    ArrayList<String> findGroupChatMemberNames(UUID groupId);

    //void deleteChats(UUID userId, DeleteChatsRequest deleteChatsRequest, boolean forBoth);

    //  private
    ArrayList<PrivateChatResponse> findPrivateChats(UUID userId);
    PrivateChatResponse findPrivateChatById(UUID userId, UUID targetUserId);
    void addPrivateChat(UUID userId, AddChatRequest privateChatDTO);
    void deletePrivateChat(UUID userId, UUID targetUserId, boolean isBoth);

    //  bot
    ArrayList<BotChatResponse> findBotChats(UUID userId);
    void deleteBotChat(UUID userId, UUID botId);

    //  all
    ChatResponseDTO findAllChats(UUID userId);
    void deleteChats(UUID userId, DeleteChatsRequest deleteChatsRequest);
}
