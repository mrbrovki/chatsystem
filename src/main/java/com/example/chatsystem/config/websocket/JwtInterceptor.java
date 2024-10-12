package com.example.chatsystem.config.websocket;

import com.example.chatsystem.security.JwtService;
import com.example.chatsystem.security.MyUserDetails;
import com.example.chatsystem.security.MyUserDetailsService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;


@Component
public class JwtInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;
    private final MyUserDetailsService userDetailsService;

    @Autowired
    public JwtInterceptor(JwtService jwtService, MyUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        List<String> cookies = request.getHeaders().get("Cookie");
        String jwt = "";
        if (cookies != null) {
            for (String cookie : cookies) {
                String[] split = cookie.split("=");
                if(split[0].equals("jwt")){
                    jwt = split[1];
                }
            }
            ObjectId userId;
            try {
                userId = new ObjectId(jwtService.extractUserId(jwt));
            }catch (Exception e){
                return false;
            }

            if(!userId.toHexString().isEmpty()) {
                MyUserDetails userDetails = userDetailsService.loadUserByUserId(userId);
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                if (jwtService.validateToken(jwt, userDetails)) {
                    attributes.put("username", userDetails.getUsername());
                    attributes.put("userId", userId);
                }
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {}
}
