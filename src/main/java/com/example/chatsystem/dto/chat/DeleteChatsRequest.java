package com.example.chatsystem.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeleteChatsRequest {
    UUID[] privateChats;
    UUID[] groupChats;
    UUID[] botChats;
    boolean both;
}
