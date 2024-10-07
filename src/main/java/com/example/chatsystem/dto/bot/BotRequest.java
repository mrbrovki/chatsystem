package com.example.chatsystem.dto.bot;

import com.example.chatsystem.bot.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BotRequest {
    private String model;
    private List<Message> messages;
    private int n;
    private double temperature;
}