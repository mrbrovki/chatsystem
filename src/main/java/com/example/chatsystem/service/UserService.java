package com.example.chatsystem.service;

import com.example.chatsystem.dto.EditUserDTO;
import com.example.chatsystem.dto.chat.AddPrivateChatDTO;
import com.example.chatsystem.dto.chat.PrivateChatResponseDTO;
import com.example.chatsystem.model.User;
import org.bson.types.ObjectId;

import java.util.List;

public interface UserService {
    User create(User user);
    EditUserDTO edit(ObjectId id, EditUserDTO editUserDTO);

    List<PrivateChatResponseDTO> findAll();
    User findByUsername(String username);
    User findById(ObjectId id);


    List<String> addPrivateChatToUser(ObjectId userId, AddPrivateChatDTO privateChatDTO);

    List<String> addPrivateChatToUser(User user, ObjectId chatUserId);

    List<String> removePrivateChatFromUser(ObjectId userId, AddPrivateChatDTO privateChatDTO);

    List<ObjectId> addGroupChatToUser(ObjectId userId, ObjectId chatId);

    List<ObjectId> addGroupChatToUser(User user, ObjectId chatId);

    List<ObjectId> removeGroupChatFromUser(ObjectId userId, ObjectId chatId);

    List<ObjectId> removeGroupChatFromUser(User user, ObjectId chatId);
}
