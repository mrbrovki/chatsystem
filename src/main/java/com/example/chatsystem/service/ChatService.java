package com.example.chatsystem.service;

import com.example.chatsystem.dto.chat.*;
import com.example.chatsystem.dto.groupchat.CreateGroupRequest;
import com.example.chatsystem.model.GroupChat;
import org.bson.types.ObjectId;

import java.util.ArrayList;

public interface ChatService {
    GroupChat findById(ObjectId groupId);

    GroupChatResponse findById(ObjectId userId, ObjectId groupId);

    GroupChat updateGroupChat(GroupChat groupChat);

    GroupChat changeGroupChatHost(ObjectId userId, ObjectId chatId, String hostName);

    void deleteGroupChat(ObjectId userId, ObjectId chatId);

    GroupChat addMemberToGroupChat(ObjectId userId, ObjectId chatId, String memberName);

    GroupChat removeMemberFromGroupChat(ObjectId userId, ObjectId chatId, String memberName);

    void addUserToGroup(ObjectId userId, ObjectId chatId);

    void removeUserFromGroup(ObjectId userId, ObjectId chatId);

    GroupChat changeGroupChatName(ObjectId userId, ObjectId chatId, String newName);

    GroupChatResponse createGroupChat(ObjectId userId, CreateGroupRequest request);

    ChatResponseDTO findAllChats(ObjectId userId);

    ArrayList<PrivateChatResponse> findPrivateChats(ObjectId userId);

    PrivateChatResponse findPrivateChatByName(ObjectId userId, String username);

    ArrayList<GroupChatResponse> findGroupChats(ObjectId userId);

    ArrayList<BotChatResponse> findBotChats(ObjectId userId);

    ArrayList<String> findGroupChatMemberNames(ObjectId chatId);
}
