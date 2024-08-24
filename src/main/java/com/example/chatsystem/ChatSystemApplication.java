package com.example.chatsystem;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.chatsystem.bot.Bot;
import com.example.chatsystem.bot.ChatGPT;
import com.example.chatsystem.config.websocket.aws.AWSConfig;
import com.example.chatsystem.controller.ChatController;
import com.example.chatsystem.controller.UserController;
import com.example.chatsystem.model.User;
import com.example.chatsystem.service.ChatService;
import com.example.chatsystem.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class ChatSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatSystemApplication.class, args);
	}

	private final ChatService chatService;

	private final UserService userService;

	private final AmazonS3 s3Client;

	@Autowired
	public ChatSystemApplication(ChatService chatService, UserService userService, AmazonS3 s3Client) {
		this.chatService = chatService;
		this.userService = userService;
        this.s3Client = s3Client;
    }

	//	testing only
	@Bean
	public CommandLineRunner commandLineRunner(MongoTemplate mongoTemplate, UserController userController, ChatController chatController) {
		return args -> {
			mongoTemplate.getDb().drop();

			try {
				// Read JSON file
				InputStream inputStream = new ClassPathResource("users.json").getInputStream();
				ObjectMapper mapper = new ObjectMapper();
				List<User> users = Arrays.asList(mapper.readValue(inputStream, User[].class));

				users.forEach(user -> {
					user.setAvatar("https://api.multiavatar.com/" + user.getUsername() +".svg");
				});

				// Save users to MongoDB
				mongoTemplate.insert(users, User.class);



				List<User> foundUsers = mongoTemplate.findAll(User.class);

				List<ObjectId> objectIds = new ArrayList<>();
				for (User user : foundUsers) {
					objectIds.add(user.getUserId());
				}

				//System.out.println(userService.addPrivateChatToUser(foundUsers.get(0).getUserId(), new AddPrivateChatDTO(foundUsers.get(1).getUsername())));
				//System.out.println(userService.addPrivateChatToUser(foundUsers.get(1).getUserId(), new AddPrivateChatDTO(foundUsers.get(0).getUsername())));

				System.out.println("Users imported successfully.");

				try {
					inputStream = new ClassPathResource("bots.json").getInputStream();
					mapper = new ObjectMapper();
					List<Bot> bots = Arrays.asList(mapper.readValue(inputStream, Bot[].class));
					bots.forEach(bot -> {
						ChatGPT.bots.put(bot.getName(), bot.getInfo());
					});

					System.out.println(ChatGPT.bots);
				}catch (Exception e){
					System.out.println(e.getMessage());
				}

				/*
				List<Bucket> buckets = awsConfig.amazonS3().listBuckets().stream().toList();
				System.out.println("Buckets:");
				for (Bucket bucket : buckets) {
					System.out.println(bucket.getName());
				}


				 */
				/*
				File kittyFile = new File("/Users/mrbrovki/Desktop/github/chatsystem/kitty.jpg");
				InputStream kittyStream = new FileInputStream(kittyFile);

				ObjectMetadata objectMetadata = new ObjectMetadata();
				PutObjectRequest putObjectRequest = new PutObjectRequest("chatbucket69", "avatar.jpg", kittyStream, objectMetadata);
				s3Client.putObject(putObjectRequest);


				 */

			} catch (IOException e) {
				System.err.println("Error importing users: " + e.getMessage());
			}
		};
	}
}