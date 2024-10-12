package com.example.chatsystem.dto.user;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EditResponse {
    private String username;
    private String avatar;
}
