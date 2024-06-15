package com.example.chatsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageResponseDTO {
    private LocalDateTime timestamp;

    private String message;

    private String senderId;

    private String type;
}