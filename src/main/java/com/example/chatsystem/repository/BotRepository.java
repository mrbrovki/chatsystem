package com.example.chatsystem.repository;

import com.example.chatsystem.bot.Bot;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BotRepository extends MongoRepository<Bot, ObjectId> {
}
