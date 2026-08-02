package com.ktb.lukas.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3Service {

    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadImage(MultipartFile image) throws IOException {

        String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .contentType(image.getContentType())
                .build();

        s3Client.putObject(
                request,
                RequestBody.fromInputStream(
                        image.getInputStream(),
                        image.getSize()
                )
        );

        return getPublicUrl(fileName);
    }

    private String getPublicUrl(String fileName) {
        return String.format(
                "https://%s.s3.%s.amazonaws.com/%s",
                bucket,
                region,
                fileName
        );
    }
}