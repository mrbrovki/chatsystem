package com.example.chatsystem.dto.chat;

import com.example.chatsystem.model.ChatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupChatResponseDTO {
    private ChatType type;
    private String id;
    private List<String> members;
    private String name;
    private String host;
    private String image;
}