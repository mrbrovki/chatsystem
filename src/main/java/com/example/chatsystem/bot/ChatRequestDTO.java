package com.example.chatsystem.bot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRequestDTO {
    private String model;
    private List<Message> messages;
    private int n;
    private double temperature;
}