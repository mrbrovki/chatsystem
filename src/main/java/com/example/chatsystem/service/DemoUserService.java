package com.example.chatsystem.service;

import org.bson.types.ObjectId;

public interface DemoUserService {
    ObjectId findAvailableUserId();
}