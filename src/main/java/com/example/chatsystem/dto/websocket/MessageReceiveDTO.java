package com.example.chatsystem.dto.websocket;

import com.example.chatsystem.model.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageReceiveDTO {
    private long timestamp;
    private String content;
    private UUID senderId;
    private MessageType type;
}