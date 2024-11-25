package com.example.chatsystem.repository;

import com.example.chatsystem.bot.Bot;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface BotRepository extends MongoRepository<Bot, UUID> {
}
