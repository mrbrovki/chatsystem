package com.example.chatsystem.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.example.chatsystem.dto.ChatResponseDTO;
import com.example.chatsystem.dto.GroupChatCreateDTO;
import com.example.chatsystem.exception.DocumentNotFoundException;
import com.example.chatsystem.model.GroupChat;
import com.example.chatsystem.model.MessageType;
import com.example.chatsystem.model.User;
import com.example.chatsystem.repository.ChatRepository;
import com.example.chatsystem.service.ChatService;
import com.example.chatsystem.service.UserService;
import com.example.chatsystem.service.WebSocketService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {
    private final ChatRepository chatRepository;
    private final UserService userService;
    private final WebSocketService webSocketService;
    private final AmazonS3 s3Client;
    @Autowired
    public ChatServiceImpl(ChatRepository chatRepository, UserService userService, WebSocketService webSocketService, AmazonS3 s3Client) {
        this.chatRepository = chatRepository;
        this.userService = userService;
        this.webSocketService = webSocketService;
        this.s3Client = s3Client;
    }

    @Override
    public GroupChat findById(ObjectId id) {
        return chatRepository.findById(id).orElseThrow(()->new DocumentNotFoundException("Chat " + id.toHexString() + " not found"));
    }

    @Override
    public ChatResponseDTO findById(ObjectId userId, ObjectId groupId) {
        GroupChat groupChat = findById(groupId);
        User user = userService.findById(groupChat.getHostId());

        if(groupChat.getMemberIds().contains(userId)){
            return ChatResponseDTO.builder()
                    .id(groupId.toHexString())
                    .members(groupChat.getMemberIds().stream().map(ObjectId::toHexString).collect(Collectors.toList()))
                    .avatar(groupChat.getAvatar())
                    .name(groupChat.getName())
                    .type(MessageType.GROUP)
                    .host(user.getUsername())
                    .build();
        }

        return null;
    }

    @Override
    public GroupChat updateGroupChat(GroupChat groupChat) {
        return chatRepository.update(groupChat);
    }

    @Override
    public GroupChat changeGroupChatHost(ObjectId userId, ObjectId chatId, String hostName) {
        GroupChat groupChat = findById(chatId);
        User newHostUser = userService.findByUsername(hostName);
        if(isHost(groupChat, userId)){
            groupChat.setHostId(newHostUser.getUserId());
        }
        return updateGroupChat(groupChat);
    }

    @Override
    public void deleteGroupChat(ObjectId userId, ObjectId chatId) {
        GroupChat groupChat = findById(chatId);
        if (isHost(groupChat, userId)) {
            List<ObjectId> memberIds = groupChat.getMemberIds();
            for (ObjectId memberId : memberIds) {
                webSocketService.unsubscribeUserToGroup(userService.findById(memberId).getUsername(), chatId);
            }
            chatRepository.delete(groupChat);
        }
    }

    @Override
    public GroupChat addMemberToGroupChat(ObjectId userId, ObjectId chatId, String memberName) {
        GroupChat groupChat = findById(chatId);
        User member = userService.findByUsername(memberName);

        if(isHost(groupChat, userId)){
            addUserToGroup(member, groupChat);
        }
        return updateGroupChat(groupChat);
    }

    @Override
    public GroupChat removeMemberFromGroupChat(ObjectId userId, ObjectId chatId, String memberName) {
        GroupChat groupChat = findById(chatId);
        User member = userService.findByUsername(memberName);

        if(isHost(groupChat, userId)){
            removeUserFromGroup(member, groupChat);
        }
        return updateGroupChat(groupChat);
    }

    @Override
    public void addUserToGroup(ObjectId userId, ObjectId chatId){
        GroupChat groupChat = findById(chatId);
        User user = userService.findById(userId);
        addUserToGroup(user, groupChat);
    }

    @Override
    public void removeUserFromGroup(ObjectId userId, ObjectId chatId){
        GroupChat groupChat = findById(chatId);
        User user = userService.findById(userId);
        removeUserFromGroup(user, groupChat);
    }

    private void addUserToGroup(User user, GroupChat groupChat) {
        groupChat.getMemberIds().add(user.getUserId());
        userService.addGroupChatToUser(user, groupChat.getId());
        webSocketService.subscribeUserToGroup(user.getUsername(), groupChat.getId());
    }

    private void removeUserFromGroup(User user, GroupChat groupChat){
        groupChat.getMemberIds().remove(user.getUserId());
        userService.removeGroupChatFromUser(user, groupChat.getId());
        webSocketService.unsubscribeUserToGroup(user.getUsername(), groupChat.getId());
    }

    @Override
    public GroupChat changeGroupChatName(ObjectId userId, ObjectId chatId, String newName) {
        GroupChat groupChat = findById(chatId);
        if(isHost(groupChat, userId)) {
            groupChat.setName(newName);
        }
        return updateGroupChat(groupChat);
    }

    @Override
    public ChatResponseDTO createGroupChat(ObjectId userId, GroupChatCreateDTO groupChatCreateDTO) {
        GroupChat groupChat = GroupChat.builder()
                .name(groupChatCreateDTO.getName())
                .avatar(groupChatCreateDTO.getAvatar())
                .hostId(userId)
                .build();

        User hostUser = userService.findById(userId);

        List<ObjectId> memberIds = new ArrayList<>();

        for (String memberName : groupChatCreateDTO.getMemberNames()) {
            memberIds.add(userService.findByUsername(memberName).getUserId());
        }

        memberIds.add(userId);

        groupChat.setMemberIds(memberIds);

        GroupChat createdGroupchat = chatRepository.save(groupChat);

        for (ObjectId memberId : memberIds) {
            User user = userService.findById(memberId);
            userService.addGroupChatToUser(user, createdGroupchat.getId());
            webSocketService.subscribeUserToGroup(user.getUsername(), createdGroupchat.getId());
        }

        return ChatResponseDTO.builder()
                .id(createdGroupchat.getId().toHexString())
                .members(createdGroupchat.getMemberIds().stream().map(ObjectId::toHexString).collect(Collectors.toList()))
                .avatar(createdGroupchat.getAvatar())
                .name(createdGroupchat.getName())
                .type(MessageType.GROUP)
                .host(hostUser.getUsername())
                .build();
    }

    @Override
    public ArrayList<ChatResponseDTO> findAllChats(ObjectId userId) {
        ArrayList<ChatResponseDTO> chatDTOs = new ArrayList<>();
        User user = userService.findById(userId);
        setUserPrivateChats(chatDTOs, user);
        setUserGroupChats(chatDTOs, user);
        System.out.println(chatDTOs);
        return chatDTOs;
    }

    @Override
    public ArrayList<ChatResponseDTO> findPrivateChats(ObjectId userId) {
        ArrayList<ChatResponseDTO> chatDTOs = new ArrayList<>();
        User user = userService.findById(userId);
        setUserPrivateChats(chatDTOs, user);
        return chatDTOs;
    }

    @Override
    public ArrayList<ChatResponseDTO> findGroupChats(ObjectId userId) {
        ArrayList<ChatResponseDTO> chatDTOs = new ArrayList<>();
        User user = userService.findById(userId);
        setUserGroupChats(chatDTOs, user);
        return chatDTOs;
    }

    @Override
    public ArrayList<String> findGroupChatMemberNames(ObjectId chatId){
        ArrayList<String> memberNames = new ArrayList<>();
        GroupChat groupChat = findById(chatId);
        groupChat.getMemberIds().forEach(memberId -> {
            User user = userService.findById(memberId);
            memberNames.add(user.getUsername());
        });
        return memberNames;
    }



    private boolean isHost(GroupChat groupChat, ObjectId userId){
        return groupChat.getHostId().equals(userId);
    }

    private void setUserPrivateChats(ArrayList<ChatResponseDTO> chatDTOs, User user) {
        List<ObjectId> chatUserIds = user.getChats();

        for (ObjectId chatUserId : chatUserIds) {
            User receiver = userService.findById(chatUserId);
            GetObjectRequest getObjectRequest = new GetObjectRequest("chatbucket69",
                    "avatar_"+chatUserId.toHexString());
            S3Object obj = s3Client.getObject(getObjectRequest);
             //inputStream = obj.getObjectContent();

            chatDTOs.add(ChatResponseDTO.builder()
                            .name(receiver.getUsername())
                            .avatar(receiver.getAvatar())


                            .type(MessageType.PRIVATE)
                            .build());

        }
    }

    private void setUserGroupChats(ArrayList<ChatResponseDTO> chatDTOs, User user) {
        List<ObjectId> groupChatIds = user.getGroupChats();
        for (ObjectId chatId : groupChatIds) {
            GroupChat groupChat = findById(chatId);
            User hostUser = userService.findById(groupChat.getHostId());
            chatDTOs.add(ChatResponseDTO.builder()
                    .id(chatId.toHexString())
                    .members(groupChat.getMemberIds().stream().map(ObjectId::toHexString).collect(Collectors.toList()))
                    .avatar(groupChat.getAvatar())
                    .name(groupChat.getName())
                    .type(MessageType.GROUP)
                    .host(hostUser.getUsername())
                    .build());
        }
    }

    public static String getPrivateChatCollectionName(ObjectId senderId, ObjectId receiverId){
        String collectionName = "chat_";
        System.out.println(senderId);
        System.out.println(receiverId);
        if(senderId.getTimestamp() < receiverId.getTimestamp()){
            collectionName += senderId + "&" + receiverId;
        }else if(senderId.getTimestamp() > receiverId.getTimestamp()){
            collectionName += receiverId + "&" + senderId;
        }else{
            int result = senderId.compareTo(receiverId);
            if(result < 0){
                collectionName += senderId + "&" + receiverId;
            }else if(result > 0){
                collectionName += receiverId + "&" + senderId;
            }else{
                collectionName += senderId;
            }
        }
        return collectionName;
    }
}
