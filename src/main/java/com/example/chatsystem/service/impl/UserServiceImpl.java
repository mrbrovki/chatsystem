package com.example.chatsystem.service.impl;

import com.example.chatsystem.dto.AddPrivateChatDTO;
import com.example.chatsystem.exception.DocumentNotFoundException;
import com.example.chatsystem.model.User;
import com.example.chatsystem.repository.UserRepository;
import com.example.chatsystem.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User create(User user){
        return userRepository.save(user);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(ObjectId id) {
        return userRepository.findById(id).orElseThrow(()->new DocumentNotFoundException("User " + id.toHexString() + " not found!"));
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(()->new DocumentNotFoundException("User " + username + " not found!"));
    }

    @Override
    public List<String> addPrivateChatToUser(ObjectId userId, AddPrivateChatDTO privateChatDTO) {
        User user = findById(userId);
        List<ObjectId> chats = user.getChats();
        User targetUser = findByUsername(privateChatDTO.getUsername());
        chats.add(targetUser.getUserId());
        userRepository.update(user);
        return chats.stream().map((chatUserId)->findById(chatUserId).getUsername()).toList();
    }

    @Override
    public List<String> addPrivateChatToUser(ObjectId userId,  ObjectId chatUserId) {
        User user = findById(userId);
        List<ObjectId> chats = user.getChats();
        User targetUser = findById(chatUserId);
        chats.add(targetUser.getUserId());
        userRepository.update(user);
        return chats.stream().map((id)->findById(id).getUsername()).toList();
    }

    @Override
    public List<String> removePrivateChatFromUser(ObjectId userId, AddPrivateChatDTO privateChatDTO) {
        User user = findById(userId);
        List<ObjectId> chats = user.getChats();
        User targetUser = findByUsername(privateChatDTO.getUsername());
        chats.remove(targetUser.getUserId());
        userRepository.update(user);
        return chats.stream().map((chatUserId)->findById(chatUserId).getUsername()).toList();
    }


    @Override
    public List<ObjectId> addGroupChatToUser(ObjectId userId, ObjectId chatId) {
        return addGroupChatToUser(findById(userId), chatId);
    }

    @Override
    public List<ObjectId> addGroupChatToUser(User user, ObjectId chatId) {
        List<ObjectId> chats = user.getGroupChats();
        chats.add(chatId);
        userRepository.update(user);
        return chats;
    }

    @Override
    public List<ObjectId> removeGroupChatFromUser(ObjectId userId, ObjectId chatId) {
        return removeGroupChatFromUser(findById(userId), chatId);
    }

    @Override
    public List<ObjectId> removeGroupChatFromUser(User user, ObjectId chatId) {
        List<ObjectId> chats = user.getGroupChats();
        chats.remove(chatId);
        userRepository.update(user);
        return chats;
    }
}
