package com.example.chatsystem.dto.websocket;


import com.example.chatsystem.model.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageSendDTO {
    private String content;
    private String receiverName;
    private MessageType type;
}