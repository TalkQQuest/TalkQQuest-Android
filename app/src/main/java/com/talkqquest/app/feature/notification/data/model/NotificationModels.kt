package com.talkqquest.app.feature.notification.data.model

import kotlinx.serialization.Serializable

// GET /api/v1/notifications 응답 data = { notifications: [...] }.
@Serializable
data class NotificationsResponse(
    val notifications: List<NotificationItemDto> = emptyList(),
)

@Serializable
data class NotificationItemDto(
    val id: String = "",
    val type: String = "",         // mission_reminder | report_ready | ...
    val title: String = "",
    val body: String? = null,
    val isRead: Boolean = false,
    val createdAt: String = "",    // ISO-8601 형식.
    // 알림이 가리키는 원본 리소스 정보. 리포트 알림 이동 등에 사용한다.
    val referenceId: String? = null,
    val referenceType: String? = null,
)

// GET/PATCH /api/v1/notifications/settings 응답 모델.
// 알림 설정 화면에서 사용하는 푸시/미션 리마인드 설정값이다.
@Serializable
data class NotificationSettings(
    val missionReminder: Boolean = false,
    val communityApproved: Boolean = false,
    val reportReady: Boolean = false,
    val marketing: Boolean = false,
    val missionReminderTime: String = "09:00",
)

@Serializable
data class NotificationSettingsUpdateRequest(
    val missionReminder: Boolean? = null,
    val communityApproved: Boolean? = null,
    val reportReady: Boolean? = null,
    val marketing: Boolean? = null,
    val missionReminderTime: String? = null,
)




data class NotificationUiItem(
    val id: String,
    val category: String,
    val body: String,
    val timeText: String,
    val isUnread: Boolean,
    val hasLink: Boolean = false,
    // 알림이 가리키는 원본 리소스 정보. 리포트 알림 이동 등에 사용한다.
    val linkReportId: String? = null,
)
