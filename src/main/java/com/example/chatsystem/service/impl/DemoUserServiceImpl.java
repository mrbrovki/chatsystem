package com.example.chatsystem.service.impl;

import com.example.chatsystem.model.DemoUser;
import com.example.chatsystem.repository.DemoUserRepository;
import com.example.chatsystem.service.DemoUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DemoUserServiceImpl implements DemoUserService {
    private final DemoUserRepository demoUserRepository;

    @Autowired
    public DemoUserServiceImpl(DemoUserRepository demoUserRepository) {
        this.demoUserRepository = demoUserRepository;
    }

    @Override
    public UUID findAvailableUserId() {
        DemoUser demoUser = demoUserRepository.findFirstByAvailableIsTrue();
        demoUser.setAvailable(false);
        demoUserRepository.save(demoUser);
        return demoUser.getId();
    }
}
