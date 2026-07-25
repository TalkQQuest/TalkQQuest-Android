package com.talkqquest.app.feature.notification.data.model

import kotlinx.serialization.Serializable

// GET /api/v1/notifications 응답 data — { notifications: [...] }.
// dev 백엔드 실계약(notification.dto.ts NotificationItem) 대조 확정 — 2026-07-25.
@Serializable
data class NotificationsResponse(
    val notifications: List<NotificationItemDto> = emptyList(),
)

@Serializable
data class NotificationItemDto(
    val id: String = "",
    val type: String = "",         // mission_reminder | report_ready | ...
    val title: String = "",        // 카드 위 작은 줄 (예: "새로운 리포트가 도착했어요!")
    val body: String? = null,      // 카드 아래 줄 (nullable) — dev 실계약 필드명 (이전 추정 message에서 정정)
    val isRead: Boolean = false,
    val createdAt: String = "",    // ISO (예: 2026-07-22T09:51:57.440Z)
)

// GET/PATCH /api/v1/notifications/settings — dev NotificationSettingsResponseDto.
// ※ 알림 설정 화면이 아직 디자인에 없어 UI 미연결. API 계약만 코드에 확보.
@Serializable
data class NotificationSettings(
    val missionReminder: Boolean = false,
    val communityApproved: Boolean = false,
    val reportReady: Boolean = false,
    val marketing: Boolean = false,
)

// 화면용 모델 — 알림 카드 1장 (디자인: 위 작은 회색 줄 + 아래 굵은 줄 + 시간 + 안읽음 점).
data class NotificationUiItem(
    val id: String,
    val category: String,  // 위 작은 줄 (Body/S Gray500)
    val body: String,      // 아래 굵은 줄 (Body/L Medium Gray900)
    val timeText: String,  // 방금 / N분 전 / N시간 전 / N일 전 / yyyy.MM.dd
    val isUnread: Boolean, // true = 보라 점 표시
)
