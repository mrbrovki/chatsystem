package com.example.chatsystem.service;

import com.example.chatsystem.model.Message;
import com.example.chatsystem.model.ReadStatus;
import org.bson.types.ObjectId;

public interface ReadStatusService {

    ReadStatus getReadStatus(String collectionName, ObjectId userId);

    ReadStatus updateReadStatus(String collectionName, Message message, ObjectId userId);
}
