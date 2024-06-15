package com.example.chatsystem.service;

import com.example.chatsystem.model.User;
import org.bson.types.ObjectId;

import java.util.List;

public interface UserService {
    List<User> findAll();
    User findByUsername(String username);
    User findById(ObjectId id);

    List<String> addPrivateChatToUser(ObjectId userId, String chatId);

    List<String> addPrivateChatToUser(User user, String chatId);

    List<String> removePrivateChatFromUser(ObjectId userId, String chatId);

    List<String> removePrivateChatFromUser(User user, String chatId);

    List<ObjectId> addGroupChatToUser(ObjectId userId, ObjectId chatId);

    List<ObjectId> addGroupChatToUser(User user, ObjectId chatId);

    List<ObjectId> removeGroupChatFromUser(ObjectId userId, ObjectId chatId);

    List<ObjectId> removeGroupChatFromUser(User user, ObjectId chatId);
}
