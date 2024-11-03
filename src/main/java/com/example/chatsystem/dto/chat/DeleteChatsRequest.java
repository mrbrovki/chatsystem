package com.example.chatsystem.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeleteChatsRequest {
    String[] privateChats;
    String[] groupChats;
    String[] botChats;
    boolean both;
}
