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

@Document(collection = "groupChats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupChat {
    @Id
    @Field("_id")
    private UUID id;
    private List<UUID> memberIds;
    private String name;
    private UUID hostId;
    private String image;
}
