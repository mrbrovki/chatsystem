package com.example.chatsystem.bot;

import com.example.chatsystem.dto.MessageDTO;
import com.example.chatsystem.dto.MessageReceiveDTO;
import com.example.chatsystem.dto.MessageSendDTO;
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

    private final MessageService messageService;
    private final MyUserDetailsService userDetailsService;
    private final WebSocketService webSocketService;

    @Autowired
    public ChatGPT(MessageService messageService, MyUserDetailsService userDetailsService, WebSocketService webSocketService) {
        this.messageService = messageService;
        this.userDetailsService = userDetailsService;
        this.webSocketService = webSocketService;
    }

    public ChatResponseDTO prompt(ChatRequestDTO chatRequestDTO) {
        WebClient client = WebClient.create("https://api.pawan.krd");
        Mono<ChatResponseDTO> responseDTOMono = client.post()
                .uri("/v1/chat/completions")
                .headers(httpHeaders -> {
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                    httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                    httpHeaders.setBearerAuth(openaiApiKey);
                }).bodyValue(chatRequestDTO)
                .retrieve()
                .bodyToMono(ChatResponseDTO.class);
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
            message.setContent(element.getMessage());
            if(bots.containsKey(element.getSenderName())){
                message.setRole("assistant");
            }else{
                message.setRole("user");
            }
            messages.add(message);
        });

        ChatRequestDTO chatRequestDTO = ChatRequestDTO.builder()
                .model("gpt-3.5-unfiltered")
                .temperature(1)
                .messages(messages)
                .build();
        ChatResponseDTO chatResponseDTO = prompt(chatRequestDTO);
        Choice choice = chatResponseDTO.getChoices().getFirst();
        String messageContent = choice.getMessage().getContent();
        MessageSendDTO botSendDTO = MessageSendDTO.builder()
                .message(messageContent)
                .receiverName(senderName)
                .type(MessageType.PRIVATE)
                .build();
        //  persist bot answer
        MessageReceiveDTO botReceiveDTO = messageService.buildMessageReceiveDTO(botSendDTO, botName);
        messageService.persistMessage(botSendDTO, botReceiveDTO, botReceiveDTO.getType());

        webSocketService.sendPrivateMessage(senderName, botReceiveDTO);
    }
}

