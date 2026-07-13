package com.talkqquest.app.core.network

import com.talkqquest.app.core.datastore.TokenDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

// 모든 요청 헤더에 토큰 자동 첨부 (서버 인증: "Authorization: Bearer <accessToken>").
// 토큰이 없거나 만료(401)면 DevTokenProvider로 테스트 계정 자동 로그인 후 재시도.
// TODO(A/로그인): 로그인 화면 연동되면 자동 로그인(DevTokenProvider) 부분 제거.
class AuthInterceptor @Inject constructor(
    private val tokenDataStore: TokenDataStore,
    private val devTokenProvider: DevTokenProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var token = runBlocking { tokenDataStore.accessToken.first() }
        if (token.isNullOrBlank()) {
            token = devTokenProvider.loginAndCache() // 첫 실행: 저장된 토큰이 없음
        }

        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }
        var response = chain.proceed(request)

        // 액세스 토큰 만료(1시간) → 재로그인 1회 후 같은 요청 재시도
        if (response.code == 401) {
            val fresh = devTokenProvider.loginAndCache()
            if (!fresh.isNullOrBlank()) {
                response.close()
                response = chain.proceed(
                    chain.request().newBuilder()
                        .header("Authorization", "Bearer $fresh")
                        .build(),
                )
            }
        }
        return response
    }
}
