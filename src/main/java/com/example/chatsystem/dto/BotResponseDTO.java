package com.example.chatsystem.dto;

import com.example.chatsystem.bot.Choice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BotResponseDTO {
    private List<Choice> choices;
}