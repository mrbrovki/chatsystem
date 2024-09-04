package com.example.chatsystem.service;

import com.example.chatsystem.model.GroupChat;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GroupChatService extends MongoRepository<GroupChat, ObjectId> {
}
