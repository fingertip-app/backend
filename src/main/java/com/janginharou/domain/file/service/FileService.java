package com.janginharou.domain.file.service;

import com.janginharou.global.exception.InvalidRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileService {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @Value("${file.upload.base-url:http://localhost:8080/api/uploads}")
    private String baseUrl;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public String uploadFile(MultipartFile file, String type) {
        // 파일 유효성 검증
        if (file.isEmpty()) {
            throw new InvalidRequestException("파일이 비어있습니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidRequestException("파일 크기는 10MB를 초과할 수 없습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new InvalidRequestException("파일명이 유효하지 않습니다.");
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new InvalidRequestException("허용되지 않는 파일 형식입니다. (jpg, jpeg, png, gif, webp만 가능)");
        }

        try {
            // 업로드 디렉토리 생성
            Path uploadPath = Paths.get(uploadDir, type);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 고유한 파일명 생성
            String uniqueFilename = UUID.randomUUID().toString() + "." + extension;
            Path filePath = uploadPath.resolve(uniqueFilename);

            // 파일 저장
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // URL 반환
            String fileUrl = baseUrl + "/" + type + "/" + uniqueFilename;
            log.info("✅ 파일 저장 완료 - path: {}, url: {}", filePath, fileUrl);

            return fileUrl;
        } catch (IOException e) {
            log.error("🔴 파일 업로드 실패", e);
            throw new InvalidRequestException("파일 업로드에 실패했습니다: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}
