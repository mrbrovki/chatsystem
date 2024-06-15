package com.example.chatsystem.repository;

import com.example.chatsystem.model.User;
import org.bson.types.ObjectId;

import java.util.List;

public interface UserRepository {
    public User findByUsername(String username);
    public List<User> findAll();
    public User findById(ObjectId id);

    public User save(User user);
    public User update(User user);
    public void deleteById(ObjectId id);

    public void deleteByUsername(String username);
}