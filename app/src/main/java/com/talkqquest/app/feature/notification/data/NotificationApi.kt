package com.talkqquest.app.feature.notification.data

import com.talkqquest.app.core.network.ApiResponse
import com.talkqquest.app.feature.notification.data.model.NotificationSettings
import com.talkqquest.app.feature.notification.data.model.NotificationSettingsUpdateRequest
import com.talkqquest.app.feature.notification.data.model.NotificationsResponse
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApi {

    @GET("api/v1/notifications")
    suspend fun getNotifications(
        @Query("isRead") isRead: Boolean? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
    ): ApiResponse<NotificationsResponse>

    // 단일 알림 읽음 처리.
    @PATCH("api/v1/notifications/{notificationId}/read")
    suspend fun markRead(
        @Path("notificationId") notificationId: String,
    ): ApiResponse<MarkReadResponse>

    // 전체 알림 읽음 처리.
    @PATCH("api/v1/notifications/all/read")
    suspend fun markAllRead(): ApiResponse<MarkReadResponse>

    // 그 전에는 서버에 지우는 통로가 없어 지운 id를 DataStore에 모아 목록에서 걸러냈고,
    // 재설치하면 지운 알림이 되살아났다. 이제 서버에서 실제로 지운다.
    @DELETE("api/v1/notifications/{notificationId}")
    suspend fun deleteNotification(
        @Path("notificationId") notificationId: String,
    ): ApiResponse<DeleteNotificationResponse>

    @DELETE("api/v1/notifications")
    suspend fun deleteAllNotifications(): ApiResponse<DeleteNotificationResponse>

    @GET("api/v1/notifications/settings")
    suspend fun getSettings(): ApiResponse<NotificationSettings>

    @PATCH("api/v1/notifications/settings")
    suspend fun updateSettings(
        @Body body: NotificationSettingsUpdateRequest,
    ): ApiResponse<NotificationSettings>

    @POST("api/v1/devices/fcm-token")
    suspend fun registerFcmToken(
        @Body body: FcmTokenRequest,
    ): ApiResponse<FcmTokenResponse>
}

@Serializable
data class MarkReadResponse(
    val updatedCount: Int = 0,
)

// 삭제 응답 data — 개별은 DeleteNotificationResponseDto{notificationId, deleted},
// 전체 삭제는 data가 null(ApiResponse_null_)이라 같은 타입을 기본값으로 받는다(읽음 처리와 같은 방식).
@Serializable
data class DeleteNotificationResponse(
    val notificationId: String = "",
    val deleted: Boolean = false,
)

// POST /api/v1/devices/fcm-token 요청 body.
@Serializable
data class FcmTokenRequest(
    val fcmToken: String,
    val platform: String, // 현재 서버 명세상 android 고정.
)

@Serializable
data class FcmTokenResponse(
    val deviceId: String = "",
)

