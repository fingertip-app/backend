package com.janginharou.domain.file.controller;

import com.janginharou.domain.file.service.FileService;
import com.janginharou.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "File API", description = "파일 업로드 관련 API")
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    @Operation(summary = "파일 업로드", description = "이미지 파일을 업로드하고 URL 반환")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "profile") String type
    ) {
        log.info("🔵 파일 업로드 요청 - filename: {}, size: {} bytes, type: {}",
            file.getOriginalFilename(), file.getSize(), type);

        String fileUrl = fileService.uploadFile(file, type);

        log.info("✅ 파일 업로드 완료 - url: {}", fileUrl);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("url", fileUrl)));
    }
}
