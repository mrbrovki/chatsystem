package com.example.chatsystem.service;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.example.chatsystem.config.websocket.aws.S3File;

import java.io.InputStream;

public interface S3Service {
    PutObjectResult putObject(String bucketName, String key, InputStream inputStream, ObjectMetadata metadata);

    PutObjectResult uploadAvatar(InputStream inputStream, String key, String contentType);

    PutObjectResult uploadChatImage(InputStream inputStream, String imageType, String key);

    S3File getChatImage(String key);
}
