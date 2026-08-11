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
// ※ 이 화면(알림창)에는 설정 UI가 없다. 설정 행은 프로필 설정 화면(A담당)에 있고,
//   여기는 API 계약만 보관한다.
@Serializable
data class NotificationSettings(
    val missionReminder: Boolean = false,
    val communityApproved: Boolean = false,
    val reportReady: Boolean = false,
    val marketing: Boolean = false,
    // 미션 리마인드를 보낼 시각. "HH:mm" 24시간 표기, 서버 기본값 "09:00" (백엔드 보고 2026-08-11).
    // 앱은 저장만 하면 되고 그 시각에 실제로 알림을 보내는 건 서버가 한다 — 따로 부를 API가 없다.
    // ★형식이 어긋나면 400(VALIDATION_ERROR "missionReminderTime은 HH:mm 형식이어야 합니다")이다.
    //   실서버 호출로 확인함. "9:00"·"09:00:00"·"9시" 전부 거절되니 두 자리로 맞춰 보낼 것.
    val missionReminderTime: String = "09:00",
)

// PATCH 본문 — 부분 업데이트라 바꿀 항목만 담는다(나머지는 null이면 안 보냄).
// 응답 DTO(NotificationSettings)를 그대로 본문에 쓰면 손대지 않은 항목까지 기본값으로 덮어써서,
// 시각 하나 바꾸려다 알림 스위치가 전부 꺼진다.
@Serializable
data class NotificationSettingsUpdateRequest(
    val missionReminder: Boolean? = null,
    val communityApproved: Boolean? = null,
    val reportReady: Boolean? = null,
    val marketing: Boolean? = null,
    val missionReminderTime: String? = null,
)

// 화면용 모델 — 알림 카드 1장 (디자인: 위 작은 회색 줄 + 아래 굵은 줄 + 시간 + 안읽음 점).
data class NotificationUiItem(
    val id: String,
    val category: String,  // 위 작은 줄 (Body/S Gray500)
    val body: String,      // 아래 굵은 줄 (Body/L Medium Gray900)
    val timeText: String,  // 방금 / N분 전 / N시간 전 / N일 전 / yyyy.MM.dd
    val isUnread: Boolean, // true = 보라 점 표시
    // 주간 비교 리포트 알림만 아래 줄 옆에 화살표가 붙는다 — 눌러서 바로 리포트로 이동하는 통로라서
    // (CSS "알림창" Frame 427321769). 나머지 알림은 읽고 마는 것이라 화살표가 없다.
    val hasLink: Boolean = false,
)
