package com.example.chatsystem.service;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;

import java.io.InputStream;

public interface S3Service {
    PutObjectResult putObject(String bucketName, String key, InputStream inputStream, ObjectMetadata metadata);

    PutObjectResult uploadAvatar(InputStream inputStream, String key, String contentType);
}
