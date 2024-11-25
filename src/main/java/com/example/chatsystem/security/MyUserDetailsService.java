package com.example.chatsystem.security;

import com.example.chatsystem.model.User;
import com.example.chatsystem.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

@Component
public class MyUserDetailsService implements UserDetailsService {

    public UserRepository userRepository;

    @Autowired
    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public MyUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            User userDetails = user.get();
            return new MyUserDetails(username, userDetails.getHashedPassword(),
                    new HashSet<>(), userDetails.getId(), userDetails.getAvatar());
        }else {
            throw new UsernameNotFoundException(username);
        }
    }

    public MyUserDetails loadUserByUserId(UUID userId) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            User userDetails = user.get();
            return new MyUserDetails(userDetails.getUsername(), userDetails.getHashedPassword(),
                    new HashSet<>(), userDetails.getId(), userDetails.getAvatar());
        }else {
            throw new UsernameNotFoundException(userId.toString() + " not found");
        }
    }
}