package com.example.chatsystem.bot;

import com.example.chatsystem.dto.bot.BotRequest;
import com.example.chatsystem.dto.bot.BotResponse;
import com.example.chatsystem.dto.message.MessageDTO;
import com.example.chatsystem.dto.websocket.MessageSendDTO;
import com.example.chatsystem.exception.DocumentNotFoundException;
import com.example.chatsystem.model.MessageType;
import com.example.chatsystem.repository.BotRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
public class BotService {
    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.api.server}")
    private String openaiApiServer;

    @Value("${openai.api.endpoint}")
    private String openaiApiEndpoint;

    @Value("${openai.api.model}")
    private String model;

    private final BotRepository botRepository;

    @Autowired
    public BotService(BotRepository botRepository) {
        this.botRepository = botRepository;
    }

    public Bot createBot(Bot bot) {
        return botRepository.save(bot);
    }

    public Bot getBotById(ObjectId id) {
        return botRepository.findById(id).orElseThrow(()->new DocumentNotFoundException("Bot " + id.toHexString() + " not found!"));
    }

    public Bot getBotByName(String name) {
        Example<Bot> example = Example.of(Bot.builder().name(name).build());
        return botRepository.findOne(example).orElseThrow(()->new DocumentNotFoundException("Bot " + name + " not found!"));
    }


    public BotResponse prompt(BotRequest botRequest) {
        WebClient client = WebClient.create(openaiApiServer);
        Mono<BotResponse> responseDTOMono = client.post()
                .uri(openaiApiEndpoint)
                .headers(httpHeaders -> {
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                    httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                    httpHeaders.setBearerAuth(openaiApiKey);
                }).bodyValue(botRequest)
                .retrieve()
                .bodyToMono(BotResponse.class);
        return responseDTOMono.block();
    }

    public MessageSendDTO handleMessage(List<MessageDTO> messageDTOs, MessageSendDTO messageSendDTO, String senderName){
        String botName = messageSendDTO.getReceiverName();

        List<Message> messages = new ArrayList<>();

        //  add training message
        Bot bot = getBotByName(botName);
        messages.add(Message.builder().content(bot.getInfo()).role("user").build());

        messageDTOs.forEach(element -> {
            Message message = new Message();
            message.setContent(element.getContent());
            if(bot.getName().equals(element.getSenderName())){
                message.setRole("assistant");
            }else{
                message.setRole("user");
            }
            messages.add(message);
        });

        BotRequest botRequest = BotRequest.builder()
                .model(model)
                .messages(messages)
                .build();
        BotResponse botResponse = prompt(botRequest);

        Choice choice = botResponse.getChoices().getFirst();
        String messageContent = choice.getMessage().getContent();

        return MessageSendDTO.builder()
                .content(messageContent)
                .receiverName(senderName)
                .type(MessageType.TEXT)
                .build();
    }
}

