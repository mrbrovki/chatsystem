package com.example.chatsystem;

import com.example.chatsystem.controller.UserController;
import com.example.chatsystem.dto.GroupChatCreateDTO;
import com.example.chatsystem.model.GroupChat;
import com.example.chatsystem.model.User;
import com.example.chatsystem.service.ChatService;
import com.example.chatsystem.service.impl.ChatServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class ChatSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatSystemApplication.class, args);
	}

	private final ChatService chatService;

	@Autowired
	public ChatSystemApplication(ChatService chatService) {
		this.chatService = chatService;
	}

	//	testing only
	@Bean
	public CommandLineRunner commandLineRunner(MongoTemplate mongoTemplate, UserController userController) {
		return args -> {};
	}
}
