package com.ktb.lukas.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.ktb.lukas.exception.CustomException;
import com.ktb.lukas.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3Service {

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    // 빌드된 S3Client 빈(Bean) 주입받기
    private final S3Client s3Client;

    public URL upload(MultipartFile multipartFile, String dirName) {
        String fileName = createFileName(multipartFile.getOriginalFilename(), dirName);

        try (InputStream inputStream = multipartFile.getInputStream()) {
            // SDK v2: PutObjectRequest의 builder를 사용하여 메타데이터(ContentType 등) 설정
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(multipartFile.getContentType()) // 파일의 Content-Type 자동 지정
                    .build();

            // 메모리에 바이트 배열을 다 올리지 않고, InputStream 정보와 사이즈를 함께 전달하여 효율적 업로드
            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(inputStream, multipartFile.getSize()));

        } catch (IOException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // SDK v2 방식으로 S3에 저장된 파일 URL 얻어오기
        GetUrlRequest getUrlRequest = GetUrlRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .build();

        return s3Client.utilities().getUrl(getUrlRequest);
    }

    private String createFileName(String fileName, String dirName) {
        return dirName + "/" + UUID.randomUUID() + "_" + fileName;
    }

    public void delete(String imageUrl, String dirName) {
        try {
            // URL에서 실제 S3 Object Key(파일명) 추출 및 디코딩
            // 예: https://amazonaws.com 형태일 때
            // URL 구조에 맞게 split 인덱스나 파싱 로직을 점검해야 합니다. 아래는 기존 로직 기준 디코딩입니다.
            String filenameFromUrl = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            String keyName = URLDecoder.decode(dirName + "/" + filenameFromUrl, StandardCharsets.UTF_8);

            System.out.println("Target Key Name: " + keyName);

            // SDK v2에는 doesObjectExist가 없습니다. headObject를 던져서 파일 존재 여부를 확인합니다.
            boolean isFileExist = true;
            try {
                HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                        .bucket(bucket)
                        .key(keyName)
                        .build();
                s3Client.headObject(headObjectRequest);
            } catch (NoSuchKeyException e) {
                isFileExist = false;
            }

            System.out.println("Is File Exist: " + isFileExist);

            if (isFileExist) {
                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(keyName)
                        .build();
                s3Client.deleteObject(deleteObjectRequest);
            } else {
                throw new IllegalArgumentException("해당 이미지 파일이 없습니다.");
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 파일 삭제를 실패하였습니다.");
        }
    }
}
