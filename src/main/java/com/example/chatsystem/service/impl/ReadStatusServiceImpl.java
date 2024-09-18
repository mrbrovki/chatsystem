package com.example.chatsystem.service.impl;

import com.example.chatsystem.model.Message;
import com.example.chatsystem.model.ReadStatus;
import com.example.chatsystem.repository.ReadStatusRepository;
import com.example.chatsystem.service.ReadStatusService;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReadStatusServiceImpl implements ReadStatusService {
    private final ReadStatusRepository readStatusRepository;

    public ReadStatusServiceImpl(ReadStatusRepository readStatusRepository) {
        this.readStatusRepository = readStatusRepository;
    }

    @Override
    public List<ReadStatus> createReadStatus(String collectionName, Message message, List<ObjectId> userIds){
        collectionName = "status_" + collectionName;
        List<ReadStatus> readStatusList = new ArrayList<>();
        for (ObjectId userId : userIds) {
            ReadStatus readStatus = ReadStatus.builder()
                    .id(message.getId() + userId.toHexString())
                    .isRead(message.getSenderId().equals(userId))
                    .build();
            readStatusList.add(readStatus);
        }
        return readStatusRepository.saveAll(readStatusList, collectionName);
    }

    @Override
    public ReadStatus getReadStatus(String collectionName, Message message, ObjectId userId){
        collectionName = "status_" + collectionName;
        return readStatusRepository.findById(message.getId() + userId.toHexString(), collectionName);
    }

    @Override
    public ReadStatus setIsRead(String collectionName, Message message, ObjectId userId, boolean isRead){
        collectionName = "status_" + collectionName;
        ReadStatus readStatus = ReadStatus.builder()
                .id(message.getId() + userId.toHexString())
                .isRead(isRead)
                .build();
        return readStatusRepository.save(readStatus, collectionName);
    }
}
