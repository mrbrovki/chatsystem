package com.example.chatsystem.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "demo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemoUser {
    @Id
    private ObjectId id;
    private boolean available;
}