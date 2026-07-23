package com.talkqquest.app.core.network

import com.talkqquest.app.core.datastore.TokenDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

// 모든 요청에 저장된 accessToken을 Authorization 헤더로 붙입니다.
// accessToken이 만료되어 401이 오면 저장된 refreshToken으로 새 accessToken을 발급받고,
// 원 요청을 한 번만 다시 보냅니다. refreshToken이 없거나 재발급에 실패하면 원래 401 응답을 그대로 돌려줍니다.
class AuthInterceptor @Inject constructor(
    private val tokenDataStore: TokenDataStore,
    private val tokenRefreshClient: TokenRefreshClient,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val accessToken = runBlocking { tokenDataStore.accessToken.first() }
        val authorizedRequest = if (accessToken.isNullOrBlank()) {
            originalRequest
        } else {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        }

        val response = chain.proceed(authorizedRequest)
        if (response.code != 401 || isRefreshRequest(originalRequest)) return response

        val refreshToken = runBlocking { tokenDataStore.refreshToken.first() } ?: return response
        val refreshedAccessToken = tokenRefreshClient.refreshAccessToken(refreshToken) ?: return response

        response.close()
        return chain.proceed(
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $refreshedAccessToken")
                .build(),
        )
    }

    private fun isRefreshRequest(request: okhttp3.Request): Boolean =
        request.url.encodedPath.endsWith("/api/v1/auth/refresh")
}
