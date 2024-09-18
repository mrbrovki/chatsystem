package com.example.chatsystem.dto;

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
public class MessagesDTO {
    private Map<String, List<MessageDTO>> PRIVATE;
    private Map<String, List<MessageDTO>> GROUP;
    private Map<String, List<MessageDTO>> BOT;
}