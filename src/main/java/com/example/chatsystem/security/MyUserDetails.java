package com.example.chatsystem.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class MyUserDetails extends User implements UserDetails {
    private String userId;

    public MyUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, String userId) {
        super(username, password, authorities);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
