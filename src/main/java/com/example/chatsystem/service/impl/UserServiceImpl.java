package com.example.chatsystem.service.impl;

import com.example.chatsystem.dto.auth.JwtResponse;
import com.example.chatsystem.dto.auth.SignupRequest;
import com.example.chatsystem.dto.auth.SignupResponse;
import com.example.chatsystem.dto.chat.DeleteChatsRequest;
import com.example.chatsystem.dto.chat.PrivateChatResponse;
import com.example.chatsystem.dto.user.EditRequest;
import com.example.chatsystem.exception.DocumentNotFoundException;
import com.example.chatsystem.model.ChatType;
import com.example.chatsystem.model.User;
import com.example.chatsystem.repository.UserRepository;
import com.example.chatsystem.security.JwtService;
import com.example.chatsystem.security.MyUserDetails;
import com.example.chatsystem.service.S3Service;
import com.example.chatsystem.service.UserService;
import org.bson.types.ObjectId;
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
    public boolean delete(ObjectId id) {
        Optional<User> user = userRepository.findById(id);
        boolean isSucecess = false;

        if (user.isPresent()) {
            userRepository.delete(user.get());
            isSucecess = true;
        }
        return isSucecess;
    }

    @Override
    public JwtResponse edit(ObjectId userId, String username, EditRequest editRequest) {
        User user = userRepository.findByUsername(editRequest.getUsername())
                .orElse(User.builder().userId(userId).build());

        if(!userId.equals(user.getUserId())){
            throw new RuntimeException("Username already exists!");
        }

        HashMap<String, Object> updates = new HashMap<>();

        String newPassword = editRequest.getPassword();
        if(!newPassword.isBlank()){
            updates.put("hashedPassword", passwordEncoder.encode(newPassword));
        }

        if(!username.equals(editRequest.getUsername())){
            updates.put("username", editRequest.getUsername());
        }

        MultipartFile avatar = editRequest.getAvatar();
        try {
            if(avatar.getSize() != 0) {
                s3Service.deleteAvatar(username);
                s3Service.uploadAvatar(avatar.getInputStream(), editRequest.getUsername(), avatar.getContentType());
                updates.put("avatar", avatarsUrl + editRequest.getUsername());
            }else{
                s3Service.renameAvatar(username, editRequest.getUsername());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        userRepository.update(userId, updates);

        MyUserDetails userDetails = new MyUserDetails(editRequest.getUsername(), "", new HashSet<>(),
                userId.toHexString(), avatarsUrl + editRequest.getUsername());

        return jwtService.generateToken(userDetails);
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
    public User findById(ObjectId id) {
        return userRepository.findById(id).orElseThrow(()->new DocumentNotFoundException("User " + id.toHexString() + " not found!"));
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
    public void addPrivateChatToUser(ObjectId userId, ObjectId chatId) {
        userRepository.addChat(userId, chatId, "privateChats");
    }

    @Override
    public void removePrivateChatFromUser(ObjectId userId, ObjectId chatId) {
        userRepository.removeChat(userId, chatId, "privateChats");
    }

    @Override
    public void addGroupChatToUser(ObjectId userId, ObjectId groupId) {
        userRepository.addChat(userId, groupId, "groupChats");
    }

    @Override
    public void removeGroupChatFromUser(ObjectId userId, ObjectId groupId) {
        userRepository.removeChat(userId, groupId, "groupChats");
    }

    @Override
    public void addBotChatToUser(ObjectId userId, ObjectId botId){
        userRepository.addChat(userId, botId, "botChats");
    }

    @Override
    public void removeBotChatFromUser(ObjectId userId, ObjectId botId){
        userRepository.removeChat(userId, botId, "botChats");
    }

    @Override
    public void removeChatsFromUser(ObjectId userId, DeleteChatsRequest request) {
        userRepository.removeChats(userId, request.getPrivateChats(), request.getGroupChats(), request.getBotChats());
    }
}
