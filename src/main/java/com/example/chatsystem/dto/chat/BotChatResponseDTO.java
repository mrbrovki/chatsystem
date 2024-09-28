package com.example.chatsystem.dto.chat;
import com.example.chatsystem.model.ChatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BotChatResponseDTO {
    private String botName;
    private ChatType type;
    private String avatar;
    private long lastReadTime;
}