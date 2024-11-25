package com.example.chatsystem.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

@Getter
@Setter
public class MyUserDetails extends User implements UserDetails {
    private UUID userId;
    private String avatar;

    public MyUserDetails(String username, String password,
                         Collection<? extends GrantedAuthority> authorities, UUID userId, String avatar) {
        super(username, password, authorities);
        this.userId = userId;
        this.avatar = avatar;
    }
}
