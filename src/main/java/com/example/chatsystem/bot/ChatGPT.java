package com.example.chatsystem.bot;

import com.example.chatsystem.dto.*;
import com.example.chatsystem.model.MessageType;
import com.example.chatsystem.security.MyUserDetails;
import com.example.chatsystem.security.MyUserDetailsService;
import com.example.chatsystem.service.MessageService;
import com.example.chatsystem.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
public class ChatGPT {
    public static HashMap<String, String> bots = new HashMap<>();

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.api.server}")
    private String openaiApiServer;

    @Value("${openai.api.endpoint}")
    private String openaiApiEndpoint;

    private final MessageService messageService;
    private final MyUserDetailsService userDetailsService;
    private final WebSocketService webSocketService;

    @Autowired
    public ChatGPT(MessageService messageService, MyUserDetailsService userDetailsService, WebSocketService webSocketService) {
        this.messageService = messageService;
        this.userDetailsService = userDetailsService;
        this.webSocketService = webSocketService;
    }

    public BotResponseDTO prompt(BotRequestDTO botRequestDTO) {
        WebClient client = WebClient.create(openaiApiServer);
        Mono<BotResponseDTO> responseDTOMono = client.post()
                .uri(openaiApiEndpoint)
                .headers(httpHeaders -> {
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                    httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                    httpHeaders.setBearerAuth(openaiApiKey);
                }).bodyValue(botRequestDTO)
                .retrieve()
                .bodyToMono(BotResponseDTO.class);
        return responseDTOMono.block();
    }

    public void handleMessageToBot(MessageSendDTO messageSendDTO, String senderName) {
        String botName = messageSendDTO.getReceiverName();
        //  persist new message
        MessageReceiveDTO messageReceiveDTO = messageService.buildMessageReceiveDTO(messageSendDTO, senderName);
        messageService.persistMessage(messageSendDTO, messageReceiveDTO, messageSendDTO.getType());

        //  pull messages
        MyUserDetails userDetails = userDetailsService.loadUserByUsername(senderName);
        List<MessageDTO> messageDTOs = messageService.getChatMessages(userDetails, botName);

        List<Message> messages = new ArrayList<>();
        //  add training message
        messages.add(Message.builder().content(bots.get(botName)).role("user").build());

        messageDTOs.forEach(element -> {
            Message message = new Message();
            message.setContent(element.getContent());
            if(bots.containsKey(element.getSenderName())){
                message.setRole("assistant");
            }else{
                message.setRole("user");
            }
            messages.add(message);
        });

        BotRequestDTO chatRequestDTO = BotRequestDTO.builder()
                .model("gpt-3.5-unfiltered")
                .temperature(1)
                .messages(messages)
                .build();
        BotResponseDTO botResponseDTO = prompt(chatRequestDTO);
        Choice choice = botResponseDTO.getChoices().getFirst();
        String messageContent = choice.getMessage().getContent();
        MessageSendDTO botSendDTO = MessageSendDTO.builder()
                .content(messageContent)
                .receiverName(senderName)
                .type(MessageType.PRIVATE)
                .build();
        //  persist bot answer
        MessageReceiveDTO botReceiveDTO = messageService.buildMessageReceiveDTO(botSendDTO, botName);
        messageService.persistMessage(botSendDTO, botReceiveDTO, botReceiveDTO.getType());

        webSocketService.sendPrivateMessage(senderName, botReceiveDTO);
    }
}

