package com.example.chatsystem.repository;

import com.example.chatsystem.model.User;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findByUsername(String username);

    List<User> findAll();

    Optional<User> findById(UUID id);

    User save(User user);

    void update(UUID userId, Map<String, Object> fieldsToUpdate);

    boolean delete(User user);

    void addChat(UUID userId, UUID chatId, String set);

    void removeChats(UUID userId, UUID[] privateChats, UUID[] groupChats, UUID[] botChats);

    void removeChat(UUID userId, UUID chatId, String set);
}