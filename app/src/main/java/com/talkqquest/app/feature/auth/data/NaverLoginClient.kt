package com.talkqquest.app.feature.auth.data

import android.content.Context
import com.navercorp.nid.NidOAuth
import com.navercorp.nid.oauth.util.NidOAuthCallback
import com.talkqquest.app.BuildConfig
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object NaverLoginClient {
    fun isConfigured(): Boolean =
        BuildConfig.NAVER_CLIENT_ID.isNotBlank() && BuildConfig.NAVER_CLIENT_SECRET.isNotBlank()

    suspend fun login(context: Context): Result<String> = suspendCoroutine { continuation ->
        if (!isConfigured()) {
            continuation.resume(Result.failure(IllegalStateException("Naver login keys are not configured.")))
            return@suspendCoroutine
        }

        NidOAuth.requestLogin(context, object : NidOAuthCallback {
            override fun onSuccess() {
                val accessToken = NidOAuth.getAccessToken()
                if (accessToken.isNullOrBlank()) {
                    continuation.resume(Result.failure(IllegalStateException("Failed to get Naver access token.")))
                } else {
                    continuation.resume(Result.success(accessToken))
                }
            }

            override fun onFailure(errorCode: String, errorDesc: String) {
                continuation.resume(Result.failure(IllegalStateException(errorDesc.ifBlank { "Naver login failed." })))
            }
        })
    }
}