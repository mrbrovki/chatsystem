package com.example.chatsystem.bot;

import com.example.chatsystem.dto.bot.BotRequest;
import com.example.chatsystem.dto.bot.BotResponse;
import com.example.chatsystem.dto.message.MessageDTO;
import com.example.chatsystem.dto.websocket.MessageSendDTO;
import com.example.chatsystem.exception.DocumentNotFoundException;
import com.example.chatsystem.model.MessageType;
import com.example.chatsystem.repository.BotRepository;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
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

    public Bot getBotById(UUID id) {
        return botRepository.findById(id).orElseThrow(()->new DocumentNotFoundException("Bot " + id.toString() + " not found!"));
    }

    public Bot getBotByName(String name) {
        Example<Bot> example = Example.of(Bot.builder().name(name).build());
        return botRepository.findOne(example).orElseThrow(()->new DocumentNotFoundException("Bot " + name + " not found!"));
    }


    public BotResponse prompt2(BotRequest botRequest) {
        WebClient client = WebClient.create(openaiApiServer);
        Mono<BotResponse> responseDTOMono = client.post()
                .uri(openaiApiEndpoint)
                .headers(httpHeaders -> {
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                    httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                    httpHeaders.setBearerAuth(openaiApiKey);
                }).bodyValue(botRequest)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    System.out.println(response.logPrefix());
                    throw new RuntimeException("Bot is temporarily unavailable!");
                })
                .bodyToMono(BotResponse.class)
                .timeout(Duration.ofSeconds(120));
        return responseDTOMono.block();
    }

    public BotResponse prompt(BotRequest botRequest) {
        Client geminiClient = new Client();
        GenerateContentConfig config =
                GenerateContentConfig.builder()
                        .systemInstruction(
                                Content.fromParts(Part.fromText(botRequest.getMessages().getFirst().getContent())))
                        .build();

        GenerateContentResponse geminiResponse =
                geminiClient.models.generateContent(
                        "gemini-2.5-flash",
                        botRequest.getMessages().getLast().getContent(),
                        config);


        List<Choice> choices = new ArrayList<>();
        choices.add(Choice.builder()
                        .index(0)
                        .message(Message.builder().role("assistant").content(geminiResponse.text()).build())
                .build());

        BotResponse botResponse = BotResponse.builder()
                .choices(choices)
                .build();

        return botResponse;
    }

    public MessageSendDTO handleMessage(List<MessageDTO> messageDTOs, MessageSendDTO messageSendDTO, UUID senderId){
        UUID botId = messageSendDTO.getReceiverId();

        List<Message> messages = new ArrayList<>();

        //  add training message
        Bot bot = getBotById(botId);
        messages.add(Message.builder().content(bot.getInfo()).role("system").build());

        messageDTOs.forEach(element -> {
            Message message = new Message();
            message.setContent(element.getContent());
            if(botId.equals(element.getSenderId())){
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

        BotResponse botResponse;
        System.out.println(botRequest);
        try {
          botResponse = prompt(botRequest);
        }catch (Exception e){
            return MessageSendDTO.builder()
                    .content(e.getMessage())
                    .receiverId(senderId)
                    .type(MessageType.TEXT)
                    .build();
        }


        Choice choice = botResponse.getChoices().getFirst();
        String messageContent = choice.getMessage().getContent();

        return MessageSendDTO.builder()
                .content(messageContent)
                .receiverId(senderId)
                .type(MessageType.TEXT)
                .build();
    }
}

