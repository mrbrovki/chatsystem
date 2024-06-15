package com.example.chatsystem.service;

import com.example.chatsystem.model.GroupChat;
import com.example.chatsystem.repository.ChatRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {
    private final ChatRepository chatRepository;

    @Autowired
    public ChatServiceImpl(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Override
    public GroupChat findById(ObjectId id) {
        return chatRepository.findById(id);
    }

    @Override
    public GroupChat changeChatHost(ObjectId id, String host) {
        GroupChat groupChat = findById(id);
        groupChat.setHostId(host);
        return updateChat(groupChat);
    }

    @Override
    public void deleteChat(ObjectId id) {
        chatRepository.deleteById(id);
    }

    @Override
    public GroupChat addMemberToChat(ObjectId chatId, ObjectId memberId) {
        GroupChat groupChat = chatRepository.findById(chatId);
        groupChat.getMemberIds().add(memberId);
        return updateChat(groupChat);
    }

    @Override
    public GroupChat removeMemberFromChat(ObjectId chatId, ObjectId memberId) {
        GroupChat groupChat = chatRepository.findById(chatId);
        groupChat.getMemberIds().remove(memberId);
        return updateChat(groupChat);
    }

    @Override
    public GroupChat changeChatName(ObjectId chatId, String newName) {
        GroupChat groupChat = findById(chatId);
        groupChat.setName(newName);
        return updateChat(groupChat);
    }

    @Override
    public List<GroupChat> findAllChats() {
        return chatRepository.findAllChats();
    }

    @Override
    public GroupChat createChat(GroupChat groupChat) {
        return chatRepository.save(groupChat);
    }

    @Override
    public String createPrivateChatCollection(ObjectId senderId, ObjectId receiverId){
        String collectionName = "chat_";
        if(senderId.getTimestamp() < receiverId.getTimestamp()){
            collectionName += senderId + "_" + receiverId;
        }else{
            collectionName += receiverId + "_" + senderId;
        }
        return collectionName;
    }

    @Override
    public GroupChat updateChat(GroupChat groupChat) {
        return chatRepository.update(groupChat);
    }
}
