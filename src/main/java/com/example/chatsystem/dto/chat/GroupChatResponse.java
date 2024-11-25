package com.example.chatsystem.dto.chat;

import com.example.chatsystem.model.ChatState;
import com.example.chatsystem.model.ChatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupChatResponse {
    private ChatType type;
    private UUID id;
    private List<UUID> members;
    private String name;
    private UUID hostId;
    private String image;
    private long lastReadTime;
    private ChatState state;
}