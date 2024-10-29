package com.example.chatsystem.repository;

import com.example.chatsystem.model.User;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByUsername(String username);

    List<User> findAll();

    Optional<User> findById(ObjectId id);

    User save(User user);

    User update(User user);

    boolean delete(User user);
}