package com.example.chatsystem.service;

import com.example.chatsystem.dto.auth.SignupRequest;
import com.example.chatsystem.dto.auth.SignupResponse;
import com.example.chatsystem.dto.chat.DeleteChatsRequest;
import com.example.chatsystem.dto.chat.PrivateChatResponse;
import com.example.chatsystem.dto.user.EditRequest;
import com.example.chatsystem.dto.user.EditResponse;
import com.example.chatsystem.model.User;


import java.util.List;
import java.util.UUID;

public interface UserService {
    SignupResponse create(SignupRequest signupDTO);
    boolean delete(UUID userId);
    EditResponse edit(UUID userId, EditRequest editUserDTO);

    List<PrivateChatResponse> findAll();
    User findByUsername(String username);
    User findById(UUID userId);


    boolean doesUsernameExist(String username);

    void addPrivateChatToUser(UUID userId, UUID chatId);

    void removePrivateChatFromUser(UUID userId, UUID chatId);

    void addGroupChatToUser(UUID userId, UUID chatId);

    void removeGroupChatFromUser(UUID userId, UUID chatId);


    void addBotChatToUser(UUID userId, UUID botId);

    void removeBotChatFromUser(UUID userId, UUID botId);

    void removeChatsFromUser(UUID userId, DeleteChatsRequest request);
}
