package com.talkqquest.app.feature.notification.data

import com.talkqquest.app.core.network.ApiResponse
import com.talkqquest.app.feature.notification.data.model.NotificationSettings
import com.talkqquest.app.feature.notification.data.model.NotificationSettingsUpdateRequest
import com.talkqquest.app.feature.notification.data.model.NotificationsResponse
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

// 알림 API — 실서버 스웨거(2026-07-22) 기준. 전 엔드포인트 Bearer 필수(AuthInterceptor 자동 첨부).
// ⚠️서버가 아직 알림을 생성하지 않아 목록이 항상 빈 배열(실측) — Repository가 목업 폴백으로 채움.
interface NotificationApi {

    // 알림 목록 조회. 응답 data = { notifications: [...] }
    @GET("api/v1/notifications")
    suspend fun getNotifications(
        @Query("isRead") isRead: Boolean? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
    ): ApiResponse<NotificationsResponse>

    // 개별 읽음 처리
    @PATCH("api/v1/notifications/{notificationId}/read")
    suspend fun markRead(
        @Path("notificationId") notificationId: String,
    ): ApiResponse<MarkReadResponse>

    // 전체 읽음 처리
    @PATCH("api/v1/notifications/all/read")
    suspend fun markAllRead(): ApiResponse<MarkReadResponse>

    // 알림 설정 조회 — dev NotificationSettingsResponseDto.
    // ※ 알림 설정 화면이 아직 디자인에 없어 UI 미연결 — API 계약만 확보(2026-07-25).
    @GET("api/v1/notifications/settings")
    suspend fun getSettings(): ApiResponse<NotificationSettings>

    // 알림 설정 변경 (부분 업데이트)
    @PATCH("api/v1/notifications/settings")
    suspend fun updateSettings(
        @Body body: NotificationSettingsUpdateRequest,
    ): ApiResponse<NotificationSettings>
}

// 읽음 처리 응답 data — 서버 스키마 미문서라 최소 형태(전부 기본값)로 수용.
@Serializable
data class MarkReadResponse(
    val updatedCount: Int = 0,
)
