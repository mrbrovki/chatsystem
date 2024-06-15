package com.example.chatsystem.dto;

import com.example.chatsystem.model.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageReceiveDTO {
    private LocalDateTime timestamp;

    private String message;

    private String senderName;

    private MessageType type;
}