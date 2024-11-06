package com.example.chatsystem.repository;

import com.example.chatsystem.model.User;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByUsername(String username);

    List<User> findAll();

    Optional<User> findById(ObjectId id);

    User save(User user);

    void update(ObjectId userId, Map<String, Object> fieldsToUpdate);

    boolean delete(User user);

    void addChat(ObjectId userId, ObjectId chatId, String set);

    void removeChats(ObjectId userId, String[] privateChats, String[] groupChats, String[] botChats);

    void removeChat(ObjectId userId, ObjectId chatId, String set);
}