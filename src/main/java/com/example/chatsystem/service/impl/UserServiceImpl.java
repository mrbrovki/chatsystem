package com.example.chatsystem.service.impl;

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
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(ObjectId id) {
        return userRepository.findById(id);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public List<String> addPrivateChatToUser(ObjectId userId, String chatId) {
        return addPrivateChatToUser(findById(userId), chatId);
    }

    @Override
    public List<String> addPrivateChatToUser(User user, String chatId) {
        List<String> chats = user.getChats();
        chats.add(chatId);
        userRepository.update(user);
        return chats;
    }

    @Override
    public List<String> removePrivateChatFromUser(ObjectId userId, String chatId) {
        return removePrivateChatFromUser(findById(userId), chatId);
    }

    @Override
    public List<String> removePrivateChatFromUser(User user, String chatId) {
        List<String> chats = user.getChats();
        chats.remove(chatId);
        userRepository.update(user);
        return chats;
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
