package com.example.chatsystem.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.example.chatsystem.config.websocket.aws.S3File;
import com.example.chatsystem.service.S3Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class S3ServiceImpl implements S3Service {
    private final AmazonS3 amazonS3;

    @Value("${aws.bucket.avatars}")
    private String avatarsBucket;

    @Value("${aws.bucket.chats}")
    private String chatsBucket;

    public S3ServiceImpl(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Override
    public PutObjectResult putObject(String bucketName, String key, InputStream inputStream, ObjectMetadata metadata) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key,
                inputStream, metadata);
        return amazonS3.putObject(putObjectRequest);
    }

    @Override
    public PutObjectResult uploadAvatar(InputStream inputStream, String key, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        return putObject(avatarsBucket, key, inputStream, metadata);
    }

    @Override
    public PutObjectResult uploadChatImage(InputStream inputStream, String imageType, String key){
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(imageType);
        return putObject(chatsBucket, key, inputStream, metadata);
    }

    @Override
    public S3File getChatImage(String key) {
        S3File file = new S3File();
        GetObjectRequest getObjectRequest = new GetObjectRequest(chatsBucket, key);
        S3Object s3Object = amazonS3.getObject(getObjectRequest);
        file.setContentType(MediaType.parseMediaType(s3Object.getObjectMetadata().getContentType()));

        try {
            file.setData(s3Object.getObjectContent().readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }
}
