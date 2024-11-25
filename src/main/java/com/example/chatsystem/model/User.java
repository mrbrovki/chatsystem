package com.example.chatsystem.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.UUID;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @Field("_id")
    private UUID id;
    private String username;
    private String email;
    private String hashedPassword;
    private List<UUID> groupChats;
    private List<UUID> privateChats;
    private List<UUID> botChats;
    private String avatar;
}
