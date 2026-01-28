package com.example.chatsystem.utils;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class LocalServer {
    public static void wolLocalServer(){
        WebClient client = WebClient.create("http://host.docker.internal:7979");

        client.get().retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), response -> {
                    System.out.println("Error response: " + response.statusCode());
                    return Mono.error(new RuntimeException("Bot is currently not available! " +
                            "My server is being used for gaming🎮"));
                })
                .bodyToMono(String.class).block();
    }
}
