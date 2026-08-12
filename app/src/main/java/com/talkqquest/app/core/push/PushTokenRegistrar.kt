package com.talkqquest.app.core.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.core.network.serverCall
import com.talkqquest.app.feature.notification.data.NotificationApi
import com.talkqquest.app.feature.notification.data.FcmTokenRequest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// 이 기기의 FCM 토큰을 서버에 등록한다 (POST /devices/fcm-token).
//
// 앱엔 Firebase 의존성과 수신 서비스(TqMessagingService)가 이미 있었는데 토큰을 서버로
// 보내지 않아, 서버가 이 기기로 푸시를 쏠 방법이 없었다.
//
// 부르는 곳이 둘이다. 하나만으로는 부족하다:
//  · TqMessagingService.onNewToken — 토큰이 새로 발급/갱신될 때. 보통 설치 직후 한 번뿐이라
//    이미 설치돼 있던 기기에서는 다시 안 불린다.
//  · 앱 시작 — 지금 갖고 있는 토큰을 다시 올린다. 서버가 같은 토큰이면 갱신만 하고
//    중복 등록하지 않는다(스웨거 설명).
@Singleton
class PushTokenRegistrar @Inject constructor(
    private val notificationApi: NotificationApi,
) {
    // 앱 시작 시점엔 부를 화면이 없어 자체 스코프에서 돈다.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * 지금 발급돼 있는 토큰을 서버에 올린다. 로그인 전이면 401이 나고 조용히 끝난다.
     *
     * 토큰 조회는 리스너로 받는다 — Task를 코루틴으로 기다리려면
     * kotlinx-coroutines-play-services가 필요한데, 이 한 곳 때문에 의존성을 늘리지 않는다.
     * 실패(Play 서비스 없는 기기 등)하면 리스너가 안 불리고 그대로 끝난다 — 원래 푸시가 불가한 환경이다.
     */
    fun registerCurrentToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            register(token)
        }
    }

    /** 토큰이 새로 발급/갱신됐을 때 (TqMessagingService.onNewToken). */
    fun register(token: String) {
        if (token.isBlank()) return
        scope.launch { send(token) }
    }

    private suspend fun send(token: String) {
        // 실패해도 재시도하지 않는다 — 다음 앱 시작에서 어차피 다시 올린다.
        // 로그인 전 실행(401)이 정상 경로라 실패를 사용자에게 알리지도 않는다.
        val r = serverCall { notificationApi.registerFcmToken(FcmTokenRequest(token, platform = "android")) }
        if (r !is ApiResult.Success) Log.d(TAG, "FCM 토큰 등록 보류 — 다음 앱 시작에서 재시도")
    }

    private companion object {
        const val TAG = "PushToken"
    }
}
