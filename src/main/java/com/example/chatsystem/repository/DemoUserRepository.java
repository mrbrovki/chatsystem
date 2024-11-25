package com.example.chatsystem.repository;

import com.example.chatsystem.model.DemoUser;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface DemoUserRepository extends MongoRepository<DemoUser, UUID> {
    DemoUser findFirstByAvailableIsTrue();
}