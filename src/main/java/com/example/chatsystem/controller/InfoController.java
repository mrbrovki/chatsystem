package com.example.chatsystem.controller;

import com.example.chatsystem.dto.info.InfoResponse;
import com.example.chatsystem.repository.InfoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v4/info")
public class InfoController {
    private final InfoRepository infoRepository;

    public InfoController(InfoRepository infoRepository) {
        this.infoRepository = infoRepository;
    }
    @GetMapping
    public ResponseEntity<InfoResponse> getInfo() {
        InfoResponse infoResponse = InfoResponse.builder()
                .contact(infoRepository.findAllBySenderIdEquals("contact"))
                .projects(infoRepository.findAllBySenderIdEquals("projects"))
                .build();
        return ResponseEntity.ok(infoResponse);
    }
}
