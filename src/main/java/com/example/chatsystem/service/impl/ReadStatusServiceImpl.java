package com.example.chatsystem.service.impl;

import com.example.chatsystem.exception.DocumentNotFoundException;
import com.example.chatsystem.model.Message;
import com.example.chatsystem.model.ReadStatus;
import com.example.chatsystem.repository.ReadStatusRepository;
import com.example.chatsystem.service.ReadStatusService;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ReadStatusServiceImpl implements ReadStatusService {
    private final ReadStatusRepository readStatusRepository;

    public ReadStatusServiceImpl(ReadStatusRepository readStatusRepository) {
        this.readStatusRepository = readStatusRepository;
    }

    @Override
    public ReadStatus getReadStatus(String collectionName, ObjectId userId){
        collectionName = "status_" + collectionName;
        return readStatusRepository.findById(userId, collectionName)
                .orElse(ReadStatus.builder()
                        .id(userId)
                        .build());
    }

    @Override
    public ReadStatus updateReadStatus(String collectionName, Message message, ObjectId userId){
        collectionName = "status_" + collectionName;
        ReadStatus readStatus = getReadStatus(collectionName, userId);
        //readStatus.setLastReadMessage(message.getId());
        readStatus.setLastReadTime(Instant.now().toEpochMilli());
        return  readStatusRepository.save(readStatus, collectionName);
    }
}
