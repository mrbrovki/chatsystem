package com.example.chatsystem.repository;

import com.example.chatsystem.model.DemoUser;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DemoUserRepository extends MongoRepository<DemoUser, ObjectId> {
    DemoUser findFirstByAvailableIsTrue();
}