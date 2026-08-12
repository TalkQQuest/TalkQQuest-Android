package com.talkqquest.app.feature.notification.data

import com.talkqquest.app.core.datastore.NotificationDataStore
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.core.network.serverCall
import com.talkqquest.app.feature.notification.data.model.NotificationItemDto
import com.talkqquest.app.feature.notification.data.model.NotificationUiItem
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// 알림 Repository. 서버 → 실패/빈 목록이면 목업 폴백 (미션 패턴과 동일).
// 서버가 알림 생성을 시작하면(목록이 비지 않으면) 자동으로 실데이터로 전환되는 구조.
@Singleton
class NotificationRepository @Inject constructor(
    private val notificationApi: NotificationApi,
    private val notificationDataStore: NotificationDataStore,
) {
    // 화면에서 읽은 상태는 서버 응답을 기다리지 않고 바로 반영한다.
    // 서버 동기화가 늦거나 실패해도 같은 앱 실행 중 다시 들어왔을 때 점이 되살아나지 않게 한다.
    private val locallyReadIds = Collections.synchronizedSet(mutableSetOf<String>())
    private val locallyDeletedIds = Collections.synchronizedSet(mutableSetOf<String>())
    private val readSyncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    suspend fun getNotifications(): ApiResult<List<NotificationUiItem>> {
        locallyDeletedIds.addAll(notificationDataStore.deletedNotificationIds())
        val r = serverCall { notificationApi.getNotifications() } // 데모 스위치(DemoConfig.USE_MOCK) 시 서버 건너뛰고 목업
        if (r is ApiResult.Success && r.data.notifications.isNotEmpty()) {
            return ApiResult.Success(
                r.data.notifications
                    .filterNot { it.id in locallyDeletedIds }
                    .map { it.toUiItem().withLocalReadState() },
            )
        }
        return ApiResult.Success(
            stubNotifications.filterNot { it.id in locallyDeletedIds }.map { it.withLocalReadState() },
        ) // 서버 빈 배열/실패 → 목업
    }

    // 홈 종의 점도 알림창과 같은 읽음 상태를 사용한다.
    // 서버 읽음 반영이 아직 끝나지 않았더라도 방금 로컬에서 읽은 알림은 제외한다.
    suspend fun hasUnreadNotification(): Boolean {
        locallyDeletedIds.addAll(notificationDataStore.deletedNotificationIds())
        // 실서버의 isRead=false 필터 응답은 읽은 알림까지 false로 내려주는 문제가 있어 사용하지 않는다.
        // 알림창과 동일한 일반 목록을 받고 각 항목의 실제 isRead 값을 직접 검사한다.
        val result = serverCall { notificationApi.getNotifications() }
        return (result as? ApiResult.Success)
            ?.data
            ?.notifications
            ?.any { !it.isRead && it.id !in locallyReadIds && it.id !in locallyDeletedIds } == true
    }

    // 클릭한 알림 또는 화면을 닫을 때 현재 불러온 알림만 읽음 처리한다.
    // 전체 읽음 API는 새로 도착했지만 아직 화면에 없던 알림까지 지울 수 있어 사용하지 않는다.
    fun markRead(notificationIds: Collection<String>) {
        val ids = notificationIds.distinct()
        if (ids.isEmpty()) return

        locallyReadIds.addAll(ids)
        readSyncScope.launch {
            ids.filterNot { it.startsWith("stub-") }.forEach { id ->
                // 읽음 API는 data=null 성공 응답이라 serverCall 결과는 쓰지 않고 요청 수행만 보장한다.
                serverCall { notificationApi.markRead(id) }
            }
        }
    }

    suspend fun deleteNotification(notificationId: String) {
        locallyDeletedIds.add(notificationId)
        notificationDataStore.addDeletedNotificationIds(listOf(notificationId))
    }

    suspend fun deleteAllNotifications(notificationIds: Collection<String>) {
        locallyDeletedIds.addAll(notificationIds)
        notificationDataStore.addDeletedNotificationIds(notificationIds)
    }

    private fun NotificationUiItem.withLocalReadState(): NotificationUiItem =
        if (id in locallyReadIds) copy(isUnread = false) else this

    private fun NotificationItemDto.toUiItem() = NotificationUiItem(
        id = id,
        category = title,
        body = body.orEmpty(), // dev 실계약 필드명 body (이전 추정 message에서 정정)
        timeText = relativeTime(createdAt),
        isUnread = !isRead,
        hasLink = isWeeklyCompare,
    )

    // 주간 비교 리포트 알림인지 — 이 알림만 화살표가 붙어 눌러서 바로 리포트로 간다.
    // ★서버가 이 알림에 어떤 type을 쓰는지 아직 못 받았다. 지금까지 받은 type 목록은
    //   mission_reminder / report_ready / community_approved / event 뿐이고 주간 비교용이 없다.
    //   그래서 type에 weekly가 들어있는지를 먼저 보고, 아니면 제목 문구로 판별한다.
    //   백엔드에서 정확한 값을 받으면 이 한 곳만 그 값 비교로 바꾸면 된다.
    private val NotificationItemDto.isWeeklyCompare: Boolean
        get() = type.contains("weekly", ignoreCase = true) || title.contains("주간 비교")
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

// 서버 없이 확인용 목업 — 디자인 목업 3건 그대로 전사. 서버가 알림을 만들기 시작하면 자동으로 안 쓰임.
private val stubNotifications = listOf(
    // 주간 비교 리포트 알림 — 화살표가 붙는 유일한 종류 (CSS Frame 427321769, 목록 맨 위)
    NotificationUiItem(
        id = "stub-0",
        category = "아직 읽지 않은 주간 비교 리포트가 있어요.",
        body = "내 주간 비교 리포트 보러가기",
        timeText = "방금",
        isUnread = true,
        hasLink = true,
    ),
    NotificationUiItem(
        id = "stub-1",
        category = "새로운 리포트가 도착했어요!",
        body = "지금 바로 성장 리포트를 보러갈 수 있어요.", // 13차에서 "성장" 추가됨
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
