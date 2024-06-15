package com.example.chatsystem.config.websocket;

import com.example.chatsystem.security.JwtService;
import com.example.chatsystem.security.MyUserDetails;
import com.example.chatsystem.security.MyUserDetailsService;
import com.example.chatsystem.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class JwtInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final MyUserDetailsService userDetailsService;
    private final UserService userService;

    @Autowired
    public JwtInterceptor(JwtService jwtService, MyUserDetailsService userDetailsService, UserService userService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.SEND.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            String token = "";
            if(authHeader != null && authHeader.startsWith("Bearer ")){
                token = authHeader.substring(7);
            }
            ObjectId userId = new ObjectId(jwtService.extractUserId(token));

            if(!userId.toHexString().isEmpty()) {
                MyUserDetails userDetails = userDetailsService.loadUserByUserId(userId);
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                if(jwtService.validateToken(token, userDetails)) {
                    accessor.setUser(authenticationToken);
                }
            }
        }


        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            String token = "";
            if(authHeader != null && authHeader.startsWith("Bearer ")){
                token = authHeader.substring(7);
            }
            ObjectId userId = new ObjectId(jwtService.extractUserId(token));

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
