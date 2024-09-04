package com.example.chatsystem.service;

import com.example.chatsystem.model.Message;
import com.example.chatsystem.model.ReadStatus;
import org.bson.types.ObjectId;

import java.util.List;

public interface ReadStatusService {
    List<ReadStatus> createMessage(String collectionName, Message message, List<ObjectId> userIds);

    ReadStatus getReadStatus(String collectionName, Message message, ObjectId userId);

    ReadStatus setIsRead(String collectionName, Message message, ObjectId userId, boolean isRead);
}
