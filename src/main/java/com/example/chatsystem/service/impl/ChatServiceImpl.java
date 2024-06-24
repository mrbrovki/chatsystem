package com.example.chatsystem.service.impl;

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

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {
    private final ChatRepository chatRepository;
    private final UserService userService;
    private final WebSocketService webSocketService;

    @Autowired
    public ChatServiceImpl(ChatRepository chatRepository, UserService userService, WebSocketService webSocketService) {
        this.chatRepository = chatRepository;
        this.userService = userService;
        this.webSocketService = webSocketService;
    }

    @Override
    public GroupChat findById(ObjectId id) {
        return chatRepository.findById(id).orElseThrow(()->new DocumentNotFoundException("Chat " + id.toHexString() + " not found"));
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
    public GroupChat createGroupChat(ObjectId userId, GroupChatCreateDTO groupChatCreateDTO) {
        GroupChat groupChat = GroupChat.builder()
                .name(groupChatCreateDTO.getName())
                .hostId(userId)
                .build();

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

        return groupChat;
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
        List<String> chatIdsStr = user.getChats();

        for (String chatId : chatIdsStr) {
            String[] userIds = chatId.substring(5).split("&");
            String userId1 = userIds[0];
            String userId2 = userIds[1];

            ObjectId receiverId = userId1.equals(user.getUserId().toHexString())? new ObjectId(userId2): new ObjectId(userId1);
            User receiver = userService.findById(receiverId);
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
            chatDTOs.add(ChatResponseDTO.builder()
                    .name(groupChat.getName())
                    .id(groupChat.getId().toHexString())
                    .type(MessageType.GROUP)
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
