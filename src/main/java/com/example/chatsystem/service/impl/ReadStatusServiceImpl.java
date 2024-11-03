package com.example.chatsystem.service.impl;

import com.example.chatsystem.model.ReadStatus;
import com.example.chatsystem.repository.ReadStatusRepository;
import com.example.chatsystem.service.ReadStatusService;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;

@Service
public class ReadStatusServiceImpl implements ReadStatusService {
    private final ReadStatusRepository readStatusRepository;

    public ReadStatusServiceImpl(ReadStatusRepository readStatusRepository) {
        this.readStatusRepository = readStatusRepository;
    }

    @Override
    public void persist(ObjectId userId, String collectionName) {
        collectionName = "status_" + collectionName;
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("lastReadTime", Instant.now().toEpochMilli());
        readStatusRepository.upsert(userId, updates, collectionName);
    }

    @Override
    public ReadStatus getReadStatus(String collectionName, ObjectId userId){
        collectionName = "status_" + collectionName;
        return readStatusRepository.findById(userId, collectionName).orElse(
                ReadStatus.builder()
                        .lastReadTime(-1)
                        .build()
        );
    }

    @Override
    public void updateTimeRead(String collectionName, ObjectId userId){
        collectionName = "status_" + collectionName;

        if(readStatusRepository.collectionExists(collectionName)){
            HashMap<String, Object> updates  = new HashMap<>();
            updates.put("lastReadTime", Instant.now().toEpochMilli());
            readStatusRepository.upsert(userId, updates, collectionName);
        }
    }

    @Override
    public void updateLastMessage(String collectionName, String messageId, ObjectId userId){
        collectionName = "status_" + collectionName;

        if(readStatusRepository.collectionExists(collectionName)){
            HashMap<String, Object> updates  = new HashMap<>();
            updates.put("lastMessage", messageId);
            readStatusRepository.upsert(userId, updates, collectionName);
        }
    }

}
