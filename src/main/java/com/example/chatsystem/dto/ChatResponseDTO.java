package com.example.chatsystem.dto;

import com.example.chatsystem.model.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatResponseDTO {
    String name;
    String id;
    String avatar;
    MessageType type;
}