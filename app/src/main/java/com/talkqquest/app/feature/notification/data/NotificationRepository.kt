package com.talkqquest.app.feature.notification.data

import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.core.network.serverCall
import com.talkqquest.app.feature.notification.data.model.NotificationItemDto
import com.talkqquest.app.feature.notification.data.model.NotificationUiItem
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

// 알림 Repository. 서버 → 실패/빈 목록이면 목업 폴백 (미션 패턴과 동일).
// 서버가 알림 생성을 시작하면(목록이 비지 않으면) 자동으로 실데이터로 전환되는 구조.
@Singleton
class NotificationRepository @Inject constructor(
    private val notificationApi: NotificationApi,
) {
    suspend fun getNotifications(): ApiResult<List<NotificationUiItem>> {
        val r = serverCall { notificationApi.getNotifications() } // 데모 스위치(DemoConfig.USE_MOCK) 시 서버 건너뛰고 목업
        if (r is ApiResult.Success && r.data.notifications.isNotEmpty()) {
            return ApiResult.Success(r.data.notifications.map { it.toUiItem() })
        }
        return ApiResult.Success(stubNotifications) // 서버 빈 배열/실패 → 목업 (디자인 목업 그대로)
    }

    // 읽음 처리 — 목업 폴백 중엔 서버에 지울 대상이 없어 실패해도 무시(조용히).
    suspend fun markAllRead() {
        serverCall { notificationApi.markAllRead() }
    }

    private fun NotificationItemDto.toUiItem() = NotificationUiItem(
        id = id,
        category = title,
        body = message,
        timeText = relativeTime(createdAt),
        isUnread = !isRead,
    )
}

// ISO 시각 → 디자인 표기(방금 / N분 전 / N시간 전 / N일 전 / yyyy.MM.dd)
private fun relativeTime(iso: String): String {
    val created = runCatching { Instant.parse(iso) }.getOrNull() ?: return ""
    val d = Duration.between(created, Instant.now())
    return when {
        d.toMinutes() < 1 -> "방금"
        d.toHours() < 1 -> "${d.toMinutes()}분 전"
        d.toDays() < 1 -> "${d.toHours()}시간 전"
        d.toDays() < 7 -> "${d.toDays()}일 전"
        else -> DateTimeFormatter.ofPattern("yyyy.MM.dd")
            .format(created.atZone(ZoneId.systemDefault()).toLocalDate())
    }
}

// 서버 없이 확인용 목업 — 디자인 목업 2건 그대로 전사. 서버가 알림을 만들기 시작하면 자동으로 안 쓰임.
private val stubNotifications = listOf(
    NotificationUiItem(
        id = "stub-1",
        category = "새로운 리포트가 도착했어요!",
        body = "지금 바로 리포트를 보러갈 수 있어요.",
        timeText = "방금",
        isUnread = true,
    ),
    NotificationUiItem(
        id = "stub-2",
        category = "새로운 기기에서 로그인되었어요",
        body = "이전 기기에서는 로그아웃 됩니다.",
        timeText = "1일 전",
        isUnread = true,
    ),
)
