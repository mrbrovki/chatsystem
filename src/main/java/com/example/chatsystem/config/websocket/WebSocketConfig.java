package com.example.chatsystem.config.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private JwtInterceptor jwtInterceptor;
    private SubscriptionInterceptor subscriptionInterceptor;
    private CorsConfiguration corsConfiguration;

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    @Autowired
    public void setJwtInterceptor(JwtInterceptor jwtInterceptor, SubscriptionInterceptor subscriptionInterceptor,
                                  CorsConfiguration corsConfiguration) {
        this.subscriptionInterceptor = subscriptionInterceptor;
        this.jwtInterceptor = jwtInterceptor;
        this.corsConfiguration = corsConfiguration;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        System.out.println(corsConfiguration.getAllowedOrigins());
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins)
                .addInterceptors(jwtInterceptor);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/user", "/group");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(subscriptionInterceptor);
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(10 * 1024 * 1024);
        container.setMaxBinaryMessageBufferSize(10 * 1024 * 1024);
        return container;
    }


    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.setMessageSizeLimit(10 * 1024 * 1024);
        registry.setSendBufferSizeLimit(10 * 1024 * 1024);
        WebSocketMessageBrokerConfigurer.super.configureWebSocketTransport(registry);
    }
}
