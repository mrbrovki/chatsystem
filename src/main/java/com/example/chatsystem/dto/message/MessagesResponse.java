package com.example.chatsystem.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessagesResponse {
    private Map<String, List<MessageDTO>> PRIVATE;
    private Map<String, List<MessageDTO>> GROUP;
    private Map<String, List<MessageDTO>> BOT;
}