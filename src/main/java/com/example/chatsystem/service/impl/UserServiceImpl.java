package com.example.chatsystem.service.impl;

import com.amazonaws.services.s3.model.PutObjectResult;
import com.example.chatsystem.dto.auth.AuthRequest;
import com.example.chatsystem.dto.auth.JwtResponse;
import com.example.chatsystem.dto.auth.SignupRequest;
import com.example.chatsystem.dto.auth.SignupResponse;
import com.example.chatsystem.dto.chat.AddPrivateChatRequest;
import com.example.chatsystem.dto.chat.PrivateChatResponse;
import com.example.chatsystem.dto.user.EditRequest;
import com.example.chatsystem.exception.DocumentNotFoundException;
import com.example.chatsystem.model.ChatType;
import com.example.chatsystem.model.User;
import com.example.chatsystem.repository.UserRepository;
import com.example.chatsystem.security.AuthService;
import com.example.chatsystem.service.S3Service;
import com.example.chatsystem.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;
    private final AuthService authService;

    @Value("${aws.avatars.url}")
    private String avatarsUrl;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, S3Service s3Service, AuthService authService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.s3Service = s3Service;
        this.authService = authService;
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
    public JwtResponse edit(ObjectId userId, EditRequest editRequest) {
        User user = findById(userId);
        String newPassword = editRequest.getPassword();

        if(!newPassword.isBlank()){
            user.setHashedPassword(passwordEncoder.encode(newPassword));
        }

        MultipartFile avatar = editRequest.getAvatar();
        try {
            if(avatar.getSize() != 0) {
                PutObjectResult result = s3Service.uploadAvatar(avatar.getInputStream(), user.getUsername(), avatar.getContentType());
                user.setAvatar(avatarsUrl + user.getUsername());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        userRepository.save(user);

        AuthRequest authRequest = AuthRequest.builder()
                .username(user.getUsername())
                .password(editRequest.getPassword())
                .build();

        return authService.authenticate(authRequest);
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
    public PrivateChatResponse addPrivateChatToUser(ObjectId userId, AddPrivateChatRequest privateChatDTO) {
        User user = findById(userId);
        List<ObjectId> chats = user.getPrivateChats();
        User targetUser = findByUsername(privateChatDTO.getUsername());
        chats.add(targetUser.getUserId());
        userRepository.update(user);
        return PrivateChatResponse.builder()
                .username(targetUser.getUsername())
                .avatar(targetUser.getAvatar())
                .type(ChatType.PRIVATE)
                .build();
    }

    @Override
    public List<String> addPrivateChatToUser(User user,  ObjectId chatUserId) {
        List<ObjectId> chats = user.getPrivateChats();
        User targetUser = findById(chatUserId);
        chats.add(targetUser.getUserId());
        userRepository.update(user);
        return chats.stream().map((id)->findById(id).getUsername()).toList();
    }

    @Override
    public void removePrivateChatFromUser(User user, ObjectId chatId) {
        List<ObjectId> chats = user.getPrivateChats();
        chats.remove(chatId);
        userRepository.update(user);
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
