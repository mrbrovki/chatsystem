package com.example.chatsystem.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@Setter
public class MyUserDetails extends User implements UserDetails {
    private String userId;
    private String avatar;

    public MyUserDetails(String username, String password,
                         Collection<? extends GrantedAuthority> authorities, String userId, String avatar) {
        super(username, password, authorities);
        this.userId = userId;
        this.avatar = avatar;
    }
}
