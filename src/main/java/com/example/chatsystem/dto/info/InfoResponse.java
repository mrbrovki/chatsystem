package com.example.chatsystem.dto.info;

import com.example.chatsystem.model.Info;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InfoResponse {
    private List<Info> projects;
    private List<Info> contact;
}
