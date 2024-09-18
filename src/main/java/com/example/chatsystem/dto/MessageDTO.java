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
public class MessageDTO {
    private long timestamp;
    private String content;
    private String senderName;
    private MessageType type;
    private boolean isRead;
}
