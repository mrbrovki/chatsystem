package com.example.chatsystem.dto;

import com.example.chatsystem.model.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatResponseDTO {
    private MessageType type;
    private String id;
    private List<String> members;
    private String name;
    private String host;
    private String avatar;
}