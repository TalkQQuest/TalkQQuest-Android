package com.talkqquest.app.core.network

import com.talkqquest.app.core.datastore.TokenDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

// 모든 요청 헤더에 토큰 자동 첨부.
// TODO(백엔드 확정 후): 헤더 형식 확인 — 지금은 "Authorization: Bearer <토큰>" 가정.
class AuthInterceptor @Inject constructor(
    private val tokenDataStore: TokenDataStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenDataStore.accessToken.first() }
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }
}
