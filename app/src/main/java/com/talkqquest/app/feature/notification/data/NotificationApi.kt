package com.talkqquest.app.feature.notification.data

import com.talkqquest.app.core.network.ApiResponse
import com.talkqquest.app.feature.notification.data.model.NotificationSettings
import com.talkqquest.app.feature.notification.data.model.NotificationSettingsUpdateRequest
import com.talkqquest.app.feature.notification.data.model.NotificationsResponse
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
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
    // 설정 UI는 프로필 설정 화면(A담당)에 있고, 그쪽은 같은 값을 /users/me/settings로 읽는다.
    // 여기는 알림 기능 쪽 계약으로 남겨둔 것.
    // ⚠️설정을 한 번도 저장한 적 없는 계정은 이 두 엔드포인트가 다 404(NOT_FOUND)다 — 실서버 확인(2026-08-11).
    @GET("api/v1/notifications/settings")
    suspend fun getSettings(): ApiResponse<NotificationSettings>

    // 알림 설정 변경 (부분 업데이트) — 바꿀 항목만 담은 요청 DTO를 쓴다.
    // 응답 DTO를 그대로 보내면 안 건드린 항목까지 기본값으로 덮인다.
    @PATCH("api/v1/notifications/settings")
    suspend fun updateSettings(
        @Body body: NotificationSettingsUpdateRequest,
    ): ApiResponse<NotificationSettings>

    // 이 기기의 FCM 토큰 등록/갱신 — 2026-08-13 연동.
    // 앱에 Firebase 의존성과 수신 서비스는 이미 있었는데 토큰을 서버에 보내지 않아
    // 푸시가 올 수 있는 경로 자체가 없었다.
    // 같은 토큰을 다시 보내도 서버가 갱신만 하고 중복 등록하지 않는다(스웨거 설명).
    @POST("api/v1/devices/fcm-token")
    suspend fun registerFcmToken(
        @Body body: FcmTokenRequest,
    ): ApiResponse<FcmTokenResponse>
}

// 읽음 처리 응답 data — 서버 스키마 미문서라 최소 형태(전부 기본값)로 수용.
@Serializable
data class MarkReadResponse(
    val updatedCount: Int = 0,
)

// POST /api/v1/devices/fcm-token 요청 body.
// ★platform에 기본값 금지: Json이 encodeDefaults=false라 기본값 필드는 요청에서 빠진다(필수 필드).
@Serializable
data class FcmTokenRequest(
    val fcmToken: String,
    val platform: String, // "android" — 서버 enum에 이 값 하나뿐이지만 호출부에서 명시
)

@Serializable
data class FcmTokenResponse(
    val deviceId: String = "",
)
