package com.example.chatsystem.dto.bot;

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
public class BotResponse {
    private List<Choice> choices;
}