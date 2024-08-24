package com.example.chatsystem.service;

import com.example.chatsystem.dto.chat.ChatResponseDTO;
import com.example.chatsystem.dto.chat.GroupChatResponseDTO;
import com.example.chatsystem.dto.chat.GroupChatCreateDTO;
import com.example.chatsystem.dto.chat.PrivateChatResponseDTO;
import com.example.chatsystem.model.GroupChat;
import org.bson.types.ObjectId;

import java.util.ArrayList;

public interface ChatService {
    GroupChat findById(ObjectId groupId);

    GroupChatResponseDTO findById(ObjectId userId, ObjectId groupId);

    GroupChat updateGroupChat(GroupChat groupChat);

    GroupChat changeGroupChatHost(ObjectId userId, ObjectId chatId, String hostName);

    void deleteGroupChat(ObjectId userId, ObjectId chatId);

    GroupChat addMemberToGroupChat(ObjectId userId, ObjectId chatId, String memberName);

    GroupChat removeMemberFromGroupChat(ObjectId userId, ObjectId chatId, String memberName);

    void addUserToGroup(ObjectId userId, ObjectId chatId);

    void removeUserFromGroup(ObjectId userId, ObjectId chatId);

    GroupChat changeGroupChatName(ObjectId userId, ObjectId chatId, String newName);

    GroupChatResponseDTO createGroupChat(ObjectId userId, GroupChatCreateDTO groupChatCreateDTO);

    ChatResponseDTO findAllChats(ObjectId userId);

    ArrayList<PrivateChatResponseDTO> findPrivateChats(ObjectId userId);

    ArrayList<GroupChatResponseDTO> findGroupChats(ObjectId userId);

    ArrayList<String> findGroupChatMemberNames(ObjectId chatId);
}
