package com.example.chatsystem.service.impl;

import com.example.chatsystem.dto.auth.SignupRequest;
import com.example.chatsystem.dto.auth.SignupResponse;
import com.example.chatsystem.dto.chat.DeleteChatsRequest;
import com.example.chatsystem.dto.chat.PrivateChatResponse;
import com.example.chatsystem.dto.user.EditRequest;
import com.example.chatsystem.dto.user.EditResponse;
import com.example.chatsystem.exception.DocumentNotFoundException;
import com.example.chatsystem.model.ChatType;
import com.example.chatsystem.model.User;
import com.example.chatsystem.repository.UserRepository;
import com.example.chatsystem.security.JwtService;
import com.example.chatsystem.service.S3Service;
import com.example.chatsystem.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;
    private final JwtService jwtService;

    @Value("${aws.avatars.url}")
    private String avatarsUrl;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, S3Service s3Service, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.s3Service = s3Service;
        this.jwtService = jwtService;
    }

    @Override
    public SignupResponse create(SignupRequest request){
        User newUser = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .hashedPassword(passwordEncoder.encode(request.getPassword()))
                .groupChats(new ArrayList<>())
                .privateChats(new ArrayList<>())
                .botChats(new ArrayList<>())
                .build();
        userRepository.save(newUser);

        return SignupResponse.builder()
                .username(newUser.getUsername())
                .build();
    }

    @Override
    public boolean delete(UUID id) {
        Optional<User> user = userRepository.findById(id);
        boolean isSucecess = false;

        if (user.isPresent()) {
            userRepository.delete(user.get());
            isSucecess = true;
        }
        return isSucecess;
    }

    @Override
    public EditResponse edit(UUID userId, EditRequest editRequest) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new DocumentNotFoundException(userId + " not found!"));
        String newUsername = editRequest.getUsername();

        if(doesUsernameExist(newUsername) && !user.getUsername().equals(newUsername)) {
            throw new RuntimeException("Username already exists!");
        }

        HashMap<String, Object> updates = new HashMap<>();

        String newPassword = editRequest.getPassword();
        if(!newPassword.isBlank()){
            updates.put("hashedPassword", passwordEncoder.encode(newPassword));
        }

        if(!user.getUsername().equals(editRequest.getUsername())){
            updates.put("username", editRequest.getUsername());
        }

        MultipartFile avatar = editRequest.getAvatar();
        try {
            if(avatar.getSize() != 0) {
                s3Service.uploadAvatar(avatar.getInputStream(), userId.toString(), avatar.getContentType());
                updates.put("avatar", avatarsUrl + userId.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        userRepository.update(userId, updates);
        return EditResponse.builder().username(newUsername).avatar(avatarsUrl + userId.toString()).build();
    }

    @Override
    public List<PrivateChatResponse> findAll() {
        List<User> users = userRepository.findAll();
        List<PrivateChatResponse> privateChatResponses = new ArrayList<>();
        for (User user : users) {
            PrivateChatResponse privateChatResponse = PrivateChatResponse.builder()
                    .username(user.getUsername())
                    .avatar(user.getAvatar())
                    .type(ChatType.PRIVATE)
                    .build();
            privateChatResponses.add(privateChatResponse);
        }
        return privateChatResponses;
    }

    @Override
    public User findById(UUID id) {
        return userRepository.findById(id).orElseThrow(()->new DocumentNotFoundException("User " + id.toString() + " not found!"));
    }

    @Override
    public boolean doesUsernameExist(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(()->new DocumentNotFoundException("User " + username + " not found!"));
    }



    @Override
    public void addPrivateChatToUser(UUID userId, UUID chatId) {
        userRepository.addChat(userId, chatId, "privateChats");
    }

    @Override
    public void removePrivateChatFromUser(UUID userId, UUID chatId) {
        userRepository.removeChat(userId, chatId, "privateChats");
    }

    @Override
    public void addGroupChatToUser(UUID userId, UUID groupId) {
        userRepository.addChat(userId, groupId, "groupChats");
    }

    @Override
    public void removeGroupChatFromUser(UUID userId, UUID groupId) {
        userRepository.removeChat(userId, groupId, "groupChats");
    }

    @Override
    public void addBotChatToUser(UUID userId, UUID botId){
        userRepository.addChat(userId, botId, "botChats");
    }

    @Override
    public void removeBotChatFromUser(UUID userId, UUID botId){
        userRepository.removeChat(userId, botId, "botChats");
    }

    @Override
    public void removeChatsFromUser(UUID userId, DeleteChatsRequest request) {
        userRepository.removeChats(userId, request.getPrivateChats(), request.getGroupChats(), request.getBotChats());
    }
}
