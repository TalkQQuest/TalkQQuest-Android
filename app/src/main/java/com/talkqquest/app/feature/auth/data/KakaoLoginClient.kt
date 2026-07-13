package com.talkqquest.app.feature.auth.data

import android.content.Context
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.talkqquest.app.BuildConfig
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object KakaoLoginClient {
    fun isConfigured(): Boolean = BuildConfig.KAKAO_NATIVE_APP_KEY.isNotBlank()

    suspend fun login(context: Context): Result<String> = suspendCoroutine { continuation ->
        if (!isConfigured()) {
            continuation.resume(Result.failure(IllegalStateException("Kakao native app key is not configured.")))
            return@suspendCoroutine
        }

        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            when {
                error != null -> continuation.resume(Result.failure(error))
                token?.accessToken.isNullOrBlank() -> {
                    continuation.resume(Result.failure(IllegalStateException("Failed to get Kakao access token.")))
                }
                else -> continuation.resume(Result.success(token.accessToken))
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context, callback = callback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }
}