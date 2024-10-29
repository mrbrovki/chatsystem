package com.example.chatsystem.service;

import com.example.chatsystem.dto.auth.JwtResponse;
import com.example.chatsystem.dto.auth.SignupRequest;
import com.example.chatsystem.dto.auth.SignupResponse;
import com.example.chatsystem.dto.chat.AddPrivateChatRequest;
import com.example.chatsystem.dto.chat.PrivateChatResponse;
import com.example.chatsystem.dto.user.EditRequest;
import com.example.chatsystem.model.User;
import org.bson.types.ObjectId;

import java.util.List;

public interface UserService {
    SignupResponse create(SignupRequest signupDTO);
    boolean delete(ObjectId id);
    JwtResponse edit(ObjectId id, EditRequest editUserDTO);

    List<PrivateChatResponse> findAll();
    User findByUsername(String username);
    User findById(ObjectId id);


    boolean doesUsernameExist(String username);

    PrivateChatResponse addPrivateChatToUser(ObjectId userId, AddPrivateChatRequest privateChatDTO);

    List<String> addPrivateChatToUser(User user, ObjectId chatUserId);

    void removePrivateChatFromUser(User user, ObjectId chatId);

    List<ObjectId> addGroupChatToUser(ObjectId userId, ObjectId chatId);

    List<ObjectId> addGroupChatToUser(User user, ObjectId chatId);

    List<ObjectId> removeGroupChatFromUser(ObjectId userId, ObjectId chatId);

    List<ObjectId> removeGroupChatFromUser(User user, ObjectId chatId);
}
