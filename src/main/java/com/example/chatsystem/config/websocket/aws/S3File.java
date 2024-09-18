package com.example.chatsystem.config.websocket.aws;

import com.example.chatsystem.model.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class S3File {
    private byte[] data;
    private MessageType contentType;
}
