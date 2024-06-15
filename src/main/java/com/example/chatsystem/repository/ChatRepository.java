package com.example.chatsystem.repository;

import com.example.chatsystem.model.GroupChat;
import org.bson.types.ObjectId;

import java.util.List;

public interface ChatRepository{
    public List<GroupChat> findAllChats();

    public GroupChat findById(ObjectId id);

    public GroupChat save(GroupChat groupChat);

    public void deleteById(ObjectId id);

    public GroupChat update(GroupChat groupChat);
}
