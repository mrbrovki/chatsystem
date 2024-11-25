package com.example.chatsystem.config.websocket;

import com.example.chatsystem.service.UserService;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@Component
public class SubscriptionInterceptor implements ChannelInterceptor {
    private final UserService userService;

    public SubscriptionInterceptor( UserService userService) {
        this.userService = userService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
           Map<String, Object> attributes = accessor.getSessionAttributes();
           UUID userId = UUID.fromString(attributes.get("userId").toString());

            String[] parts = accessor.getDestination().split("/");
            if (parts[1].equals("user")) {
                String destinationUserId = parts[2];

                if (!userId.toString().equals(destinationUserId)) {
                    throw new IllegalStateException("User is not authenticated for subscription");
                }
            } else {
                String destinationChatId = parts[2];
                List<UUID> userGroupChatIds = userService.findById(userId).getGroupChats();
                if (!userGroupChatIds.contains(UUID.fromString(destinationChatId))) {
                    throw new IllegalStateException("User is not in the group for subscription");
                }
            }
        }

        return message;
    }
}