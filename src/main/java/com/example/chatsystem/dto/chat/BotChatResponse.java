package com.example.chatsystem.dto.chat;

import com.example.chatsystem.model.ChatState;
import com.example.chatsystem.model.ChatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BotChatResponse {
    private String botName;
    private ChatType type;
    private String avatar;
    private long lastReadTime;
    private ChatState state;
    private UUID id;
}