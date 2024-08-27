package com.example.chatsystem.config.websocket.aws;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class S3File {
    private byte[] data;
    private MediaType contentType;
}
