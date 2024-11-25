package com.example.chatsystem.repository;

import com.example.chatsystem.model.GroupChat;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRepository{
    List<GroupChat> findAll();

    Optional<GroupChat> findById(UUID id);

    GroupChat save(GroupChat groupChat);

    void delete(GroupChat groupChat);

    GroupChat update(GroupChat groupChat);
}
