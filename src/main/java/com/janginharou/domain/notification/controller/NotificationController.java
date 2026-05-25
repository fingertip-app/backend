package com.janginharou.domain.notification.controller;

import com.janginharou.domain.notification.dto.NotificationRequest;
import com.janginharou.domain.notification.dto.NotificationResponse;
import com.janginharou.domain.notification.service.NotificationService;
import com.janginharou.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification API", description = "알림 관련 API")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/{notificationId}")
    @Operation(summary = "알림 조회", description = "알림 ID로 알림 정보 조회")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotification(@PathVariable Long notificationId) {
        NotificationResponse response = NotificationResponse.from(notificationService.getNotificationById(notificationId));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자 알림 목록", description = "사용자의 모든 알림 조회 (최신 순)")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUserNotifications(@PathVariable Long userId) {
        List<NotificationResponse> responses = notificationService.getNotificationsByUserId(userId)
                .stream()
                .map(NotificationResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/user/{userId}/unread")
    @Operation(summary = "미읽은 알림", description = "사용자의 미읽은 알림 조회")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications(@PathVariable Long userId) {
        List<NotificationResponse> responses = notificationService.getUnreadNotificationsByUserId(userId)
                .stream()
                .map(NotificationResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/user/{userId}/unread-count")
    @Operation(summary = "미읽은 알림 개수", description = "사용자의 미읽은 알림 개수 조회")
    public ResponseEntity<ApiResponse<Long>> getUnreadNotificationCount(@PathVariable Long userId) {
        long count = notificationService.getUnreadNotificationCount(userId);
        return ResponseEntity.ok(ApiResponse.ok(count));
    }

    @PostMapping
    @Operation(summary = "알림 생성 및 발송", description = "새로운 알림 생성 및 비동기 발송")
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(@RequestBody NotificationRequest request) {
        // TODO: 알림 생성 및 비동기 발송 처리
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(null, "Notification created and sent successfully"));
    }

    @PutMapping("/{notificationId}/read")
    @Operation(summary = "알림 읽음 표시", description = "알림을 읽음으로 표시")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable Long notificationId) {
        NotificationResponse response = NotificationResponse.from(notificationService.markAsRead(notificationId));
        return ResponseEntity.ok(ApiResponse.ok(response, "Notification marked as read"));
    }

    @PutMapping("/user/{userId}/read-all")
    @Operation(summary = "모든 알림 읽음 표시", description = "사용자의 모든 미읽은 알림을 읽음으로 표시")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@PathVariable Long userId) {
        // TODO: 사용자의 모든 미읽은 알림을 읽음으로 표시
        return ResponseEntity.ok(ApiResponse.ok(null, "All notifications marked as read"));
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "알림 삭제", description = "알림 삭제")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long notificationId) {
        // TODO: 알림 삭제 처리
        return ResponseEntity.ok(ApiResponse.ok(null, "Notification deleted successfully"));
    }
}
