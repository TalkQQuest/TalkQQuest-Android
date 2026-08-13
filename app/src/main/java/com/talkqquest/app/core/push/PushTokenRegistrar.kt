package com.talkqquest.app.core.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.core.network.serverCall
import com.talkqquest.app.feature.notification.data.FcmTokenRequest
import com.talkqquest.app.feature.notification.data.NotificationApi
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// 현재 기기의 FCM 토큰을 서버에 등록한다.
// 앱 시작, 로그인 성공, Firebase 토큰 갱신 시점에서 호출된다.
@Singleton
class PushTokenRegistrar @Inject constructor(
    private val notificationApi: NotificationApi,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun registerCurrentToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            register(token)
        }
    }

    fun register(token: String) {
        if (token.isBlank()) return
        scope.launch { send(token) }
    }

    private suspend fun send(token: String) {
        val result = serverCall {
            notificationApi.registerFcmToken(
                FcmTokenRequest(
                    fcmToken = token,
                    platform = "android",
                ),
            )
        }
        if (result !is ApiResult.Success) {
            Log.d(TAG, "FCM token registration deferred")
        }
    }

    private companion object {
        const val TAG = "PushToken"
    }
}
