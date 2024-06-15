package com.example.chatsystem.service;

import com.example.chatsystem.model.User;
import com.example.chatsystem.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService{
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
    public List<String> addChatToUser(ObjectId userId, String chatId) {
        return addChatToUser(findById(userId), chatId);
    }

    @Override
    public List<String> addChatToUser(User user, String chatId) {
        List<String> chats = user.getChats();
        chats.add(chatId);
        return chats;
    }

    @Override
    public List<String> removeChatFromUser(ObjectId userId, String chatId) {
        return removeChatFromUser(findById(userId), chatId);
    }

    @Override
    public List<String> removeChatFromUser(User user, String chatId) {
        List<String> chats = user.getChats();
        chats.remove(chatId);
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
        return chats;
    }
}
