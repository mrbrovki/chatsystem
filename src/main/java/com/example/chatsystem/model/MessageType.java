package com.example.chatsystem.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.http.MediaType;
import lombok.Getter;

@Getter
public enum MessageType {
    JOIN("join"),
    LEAVE("leave"),
    IMAGE_JPEG(MediaType.IMAGE_JPEG_VALUE),
    IMAGE_PNG(MediaType.IMAGE_PNG_VALUE),
    IMAGE_GIF(MediaType.IMAGE_GIF_VALUE),
    APPLICATION_PDF(MediaType.APPLICATION_PDF_VALUE),
    APPLICATION_JSON(MediaType.APPLICATION_JSON_VALUE),
    VIDEO_AVI("video/avi"),
    VIDEO_MOV("video/mov"),
    VIDEO_MP4("video/mp4"),
    VIDEO_WEBM("video/webm"),
    TEXT(MediaType.TEXT_PLAIN_VALUE);

    private final String value;

    MessageType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static MessageType fromValue(String value) {
        for (MessageType type : MessageType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
