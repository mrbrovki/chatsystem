package com.example.chatsystem.repository;

import com.example.chatsystem.model.GroupChat;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GroupChatRepo extends MongoRepository<GroupChat, ObjectId> {
}
