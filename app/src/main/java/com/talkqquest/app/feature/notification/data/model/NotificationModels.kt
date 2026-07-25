package com.talkqquest.app.feature.notification.data.model

import kotlinx.serialization.Serializable

// GET /api/v1/notifications 응답 data — { notifications: [...] } (2026-07-22 실측).
// ⚠️항목 필드명은 추정: 서버가 아직 알림을 안 만들어 실응답이 항상 빈 배열이고
//   스웨거에도 항목 스키마가 없음. 첫 실데이터가 오면 필드명 대조해 수정할 것.
@Serializable
data class NotificationsResponse(
    val notifications: List<NotificationItemDto> = emptyList(),
)

@Serializable
data class NotificationItemDto(
    val id: String = "",
    val type: String = "",        // 추정: mission_reminder | report_ready | ... (알림 설정 필드명 기준)
    val title: String = "",       // 추정: 카드 위 작은 줄 (예: "새로운 리포트가 도착했어요!")
    val message: String = "",     // 추정: 카드 아래 굵은 줄 (예: "지금 바로 리포트를 보러갈 수 있어요.")
    val isRead: Boolean = false,
    val createdAt: String = "",   // ISO (예: 2026-07-22T09:51:57.440Z)
)

// 화면용 모델 — 알림 카드 1장 (디자인: 위 작은 회색 줄 + 아래 굵은 줄 + 시간 + 안읽음 점).
data class NotificationUiItem(
    val id: String,
    val category: String,  // 위 작은 줄 (Body/S Gray500)
    val body: String,      // 아래 굵은 줄 (Body/L Medium Gray900)
    val timeText: String,  // 방금 / N분 전 / N시간 전 / N일 전 / yyyy.MM.dd
    val isUnread: Boolean, // true = 보라 점 표시
)
