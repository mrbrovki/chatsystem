package com.example.chatsystem.service;

import com.example.chatsystem.model.GroupChat;
import org.bson.types.ObjectId;

import java.util.List;

public interface ChatService {
    public GroupChat findById(ObjectId id);

    public List<GroupChat> findAllChats();

    public GroupChat createChat(GroupChat groupChat);

    public String createPrivateChatCollection(ObjectId senderId, ObjectId receiverId);

    public GroupChat updateChat(GroupChat groupChat);

    public GroupChat changeChatHost(ObjectId id, String host);
    public void deleteChat(ObjectId id);

    public GroupChat addMemberToChat(ObjectId chatId, ObjectId memberId);

    public GroupChat removeMemberFromChat(ObjectId chatId, ObjectId memberId);

    public GroupChat changeChatName(ObjectId chatId, String newName);
}
