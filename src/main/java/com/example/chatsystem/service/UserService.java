package com.example.chatsystem.service;

import com.example.chatsystem.dto.auth.JwtResponse;
import com.example.chatsystem.dto.auth.SignupRequest;
import com.example.chatsystem.dto.auth.SignupResponse;
import com.example.chatsystem.dto.chat.DeleteChatsRequest;
import com.example.chatsystem.dto.chat.PrivateChatResponse;
import com.example.chatsystem.dto.user.EditRequest;
import com.example.chatsystem.model.User;
import org.bson.types.ObjectId;

import java.util.List;

public interface UserService {
    SignupResponse create(SignupRequest signupDTO);
    boolean delete(ObjectId id);
    JwtResponse edit(ObjectId id, String username, EditRequest editUserDTO);

    List<PrivateChatResponse> findAll();
    User findByUsername(String username);
    User findById(ObjectId id);


    boolean doesUsernameExist(String username);

    void addPrivateChatToUser(ObjectId userId, ObjectId chatId);

    void removePrivateChatFromUser(ObjectId userId, ObjectId chatId);

    void addGroupChatToUser(ObjectId userId, ObjectId chatId);

    void removeGroupChatFromUser(ObjectId userId, ObjectId chatId);


    void addBotChatToUser(ObjectId userId, ObjectId botId);

    void removeBotChatFromUser(ObjectId userId, ObjectId botId);

    void removeChatsFromUser(ObjectId userId, DeleteChatsRequest request);
}
