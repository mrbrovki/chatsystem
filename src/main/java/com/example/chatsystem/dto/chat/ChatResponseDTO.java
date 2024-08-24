package com.example.chatsystem.dto.chat;

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
    private List<PrivateChatResponseDTO> privateChats;
    private List<GroupChatResponseDTO> groupChats;
    private List<BotChatResponseDTO> botChats;
}