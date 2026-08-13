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

// ?뚮┝ API ???ㅼ꽌踰??ㅼ썾嫄?2026-07-22) 湲곗?. ???붾뱶?ъ씤??Bearer ?꾩닔(AuthInterceptor ?먮룞 泥⑤?).
// ?좑툘?쒕쾭媛 ?꾩쭅 ?뚮┝???앹꽦?섏? ?딆븘 紐⑸줉????긽 鍮?諛곗뿴(?ㅼ륫) ??Repository媛 紐⑹뾽 ?대갚?쇰줈 梨꾩?.
interface NotificationApi {

    // ?뚮┝ 紐⑸줉 議고쉶. ?묐떟 data = { notifications: [...] }
    @GET("api/v1/notifications")
    suspend fun getNotifications(
        @Query("isRead") isRead: Boolean? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
    ): ApiResponse<NotificationsResponse>

    // 媛쒕퀎 ?쎌쓬 泥섎━
    @PATCH("api/v1/notifications/{notificationId}/read")
    suspend fun markRead(
        @Path("notificationId") notificationId: String,
    ): ApiResponse<MarkReadResponse>

    // ?꾩껜 ?쎌쓬 泥섎━
    @PATCH("api/v1/notifications/all/read")
    suspend fun markAllRead(): ApiResponse<MarkReadResponse>

    // 알림 삭제 / 전체 삭제 — 2026-08-13 백엔드 신규.
    // 그 전에는 서버에 지우는 통로가 없어 지운 id를 DataStore에 모아 목록에서 걸러냈고,
    // 재설치하면 지운 알림이 되살아났다. 이제 서버에서 실제로 지운다.
    @DELETE("api/v1/notifications/{notificationId}")
    suspend fun deleteNotification(
        @Path("notificationId") notificationId: String,
    ): ApiResponse<DeleteNotificationResponse>

    @DELETE("api/v1/notifications")
    suspend fun deleteAllNotifications(): ApiResponse<DeleteNotificationResponse>

    // ?뚮┝ ?ㅼ젙 議고쉶 ??dev NotificationSettingsResponseDto.
    // ?ㅼ젙 UI???꾨줈???ㅼ젙 ?붾㈃(A?대떦)???덇퀬, 洹몄そ? 媛숈? 媛믪쓣 /users/me/settings濡??쎈뒗??
    // ?ш린???뚮┝ 湲곕뒫 履?怨꾩빟?쇰줈 ?④꺼??寃?
    // ?좑툘?ㅼ젙????踰덈룄 ??ν븳 ???녿뒗 怨꾩젙? ?????붾뱶?ъ씤?멸? ??404(NOT_FOUND)?????ㅼ꽌踰??뺤씤(2026-08-11).
    @GET("api/v1/notifications/settings")
    suspend fun getSettings(): ApiResponse<NotificationSettings>

    // ?뚮┝ ?ㅼ젙 蹂寃?(遺遺??낅뜲?댄듃) ??諛붽? ??ぉ留??댁? ?붿껌 DTO瑜??대떎.
    // ?묐떟 DTO瑜?洹몃?濡?蹂대궡硫???嫄대뱶由???ぉ源뚯? 湲곕낯媛믪쑝濡???씤??
    @PATCH("api/v1/notifications/settings")
    suspend fun updateSettings(
        @Body body: NotificationSettingsUpdateRequest,
    ): ApiResponse<NotificationSettings>

    // ??湲곌린??FCM ?좏겙 ?깅줉/媛깆떊 ??2026-08-13 ?곕룞.
    // ?깆뿉 Firebase ?섏〈?깃낵 ?섏떊 ?쒕퉬?ㅻ뒗 ?대? ?덉뿀?붾뜲 ?좏겙???쒕쾭??蹂대궡吏 ?딆븘
    // ?몄떆媛 ?????덈뒗 寃쎈줈 ?먯껜媛 ?놁뿀??
    // 媛숈? ?좏겙???ㅼ떆 蹂대궡???쒕쾭媛 媛깆떊留??섍퀬 以묐났 ?깅줉?섏? ?딅뒗???ㅼ썾嫄??ㅻ챸).
    @POST("api/v1/devices/fcm-token")
    suspend fun registerFcmToken(
        @Body body: FcmTokenRequest,
    ): ApiResponse<FcmTokenResponse>
}

// ?쎌쓬 泥섎━ ?묐떟 data ???쒕쾭 ?ㅽ궎留?誘몃Ц?쒕씪 理쒖냼 ?뺥깭(?꾨? 湲곕낯媛?濡??섏슜.
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

// POST /api/v1/devices/fcm-token ?붿껌 body.
// ?꿶latform??湲곕낯媛?湲덉?: Json??encodeDefaults=false??湲곕낯媛??꾨뱶???붿껌?먯꽌 鍮좎쭊???꾩닔 ?꾨뱶).
@Serializable
data class FcmTokenRequest(
    val fcmToken: String,
    val platform: String, // "android" ???쒕쾭 enum????媛??섎굹肉먯씠吏留??몄텧遺?먯꽌 紐낆떆
)

@Serializable
data class FcmTokenResponse(
    val deviceId: String = "",
)

