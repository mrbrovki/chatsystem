package com.example.chatsystem.repository;

import com.example.chatsystem.model.GroupChat;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface GroupChatRepo extends MongoRepository<GroupChat, UUID> {
}
