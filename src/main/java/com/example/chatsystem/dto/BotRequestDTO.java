package com.example.chatsystem.dto;

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
public class BotRequestDTO {
    private String model;
    private List<Message> messages;
    private int n;
    private double temperature;
}