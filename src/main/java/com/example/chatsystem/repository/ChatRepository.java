package com.example.chatsystem.repository;

import com.example.chatsystem.model.GroupChat;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

public interface ChatRepository{
    List<GroupChat> findAll();

    Optional<GroupChat> findById(ObjectId id);

    GroupChat save(GroupChat groupChat);

    void delete(GroupChat groupChat);

    GroupChat update(GroupChat groupChat);
}
