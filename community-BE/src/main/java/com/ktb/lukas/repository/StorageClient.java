package com.ktb.lukas.repository;

import org.springframework.web.multipart.MultipartFile;

public interface StorageClient {
    String upload(MultipartFile file, String fileName);
    void delete(String fileName);
}
