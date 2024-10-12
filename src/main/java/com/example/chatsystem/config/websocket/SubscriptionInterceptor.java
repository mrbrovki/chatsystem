package com.example.chatsystem.config.websocket;

import com.example.chatsystem.security.MyUserDetails;
import com.example.chatsystem.security.MyUserDetailsService;
import com.example.chatsystem.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Component
public class SubscriptionInterceptor implements ChannelInterceptor {
    private final MyUserDetailsService userDetailsService;
    private final UserService userService;

    public SubscriptionInterceptor(MyUserDetailsService userDetailsService, UserService userService) {
        this.userDetailsService = userDetailsService;
        this.userService = userService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
           Map<String, Object> attributes = accessor.getSessionAttributes();
           ObjectId userId = new ObjectId(attributes.get("userId").toString());

            String[] parts = accessor.getDestination().split("/");
            if (parts[1].equals("user")) {
                String destinationUsername = parts[2];
                MyUserDetails userDetails = userDetailsService.loadUserByUserId(userId);

                if (!userDetails.getUsername().equals(destinationUsername)) {
                    throw new IllegalStateException("User is not authenticated for subscription");
                }
            } else {
                String destinationChatId = parts[2];
                List<ObjectId> userGroupChatIds = userService.findById(userId).getGroupChats();
                if (!userGroupChatIds.contains(new ObjectId(destinationChatId))) {
                    throw new IllegalStateException("User is not in the group for subscription");
                }
            }
        }

        return message;
    }
}