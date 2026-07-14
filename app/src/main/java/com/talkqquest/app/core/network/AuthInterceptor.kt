package com.talkqquest.app.core.network

import com.talkqquest.app.core.datastore.TokenDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

// 모든 요청 헤더에 토큰 자동 첨부 (서버 인증: "Authorization: Bearer <accessToken>").
//
// 로그인 화면(A담당)이 시작점이 되기 전까지, 토큰이 없으면 테스트 계정으로 자동 로그인한다.
// 서버 accessToken은 1시간 만료라 401 복구가 필요한데, 이때 "누구의 토큰인지" 반드시 구분한다:
//
//   refreshToken 있음 = 실제 로그인한 사용자 → 절대 건드리지 않는다.
//        (테스트 계정으로 재로그인하면 남의 세션을 덮어써 다른 사람으로 바뀜)
//   refreshToken 없음 = DevTokenProvider가 넣은 개발용 토큰 → 만료 시 재로그인해 복구.
//
// (DevTokenProvider는 saveAccessToken만, 실제 로그인은 saveTokens로 refreshToken까지 저장 — 이 차이로 판별)
// TODO(A/로그인): 로그인 흐름이 완성되면 DevTokenProvider와 아래 분기를 통째로 삭제.
//   실제 사용자의 401 만료 복구는 refreshToken으로 재발급하는 로직이 대신한다.
class AuthInterceptor @Inject constructor(
    private val tokenDataStore: TokenDataStore,
    private val devTokenProvider: DevTokenProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var token = runBlocking { tokenDataStore.accessToken.first() }
        if (token.isNullOrBlank()) {
            token = devTokenProvider.loginAndCache() // 아직 아무도 로그인 안 한 상태
        }

        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }
        val response = chain.proceed(request)

        // 401(만료) — 개발용 토큰일 때만 재로그인해 복구. 실제 로그인 세션은 손대지 않는다.
        if (response.code != 401) return response
        val isRealUserSession = runBlocking { tokenDataStore.refreshToken.first() } != null
        if (isRealUserSession) return response

        val fresh = devTokenProvider.loginAndCache() ?: return response
        response.close()
        return chain.proceed(
            chain.request().newBuilder()
                .header("Authorization", "Bearer $fresh")
                .build(),
        )
    }
}
