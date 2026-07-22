package com.talkqquest.app.core.network

import com.talkqquest.app.core.datastore.TokenDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

// 모든 요청 헤더에 토큰 자동 첨부 (서버 인증: "Authorization: Bearer <accessToken>").
//
// [개발용 임시 장치 안내 — 로그인 담당자분 참고용으로 남겨둡니다]
// 로그인 화면이 앱 시작점이 되기 전까지, 토큰이 없으면 팀 테스트 계정으로 자동 로그인해서
// (DevTokenProvider) 홈·미션 등 API가 로그인 없이도 동작하게 해둔 상태입니다.
// 서버 accessToken이 1시간 만료라 401 복구가 필요한데, 이때 "누구의 토큰인지"를 구분합니다:
//
//   refreshToken 있음 = 화면에서 실제 로그인한 사용자 → 건드리지 않습니다.
//        (여기서 테스트 계정으로 재로그인하면 사용자 세션이 테스트 계정으로 바뀌는 사고가 나서요)
//        참고: 현재 실사용자 토큰은 만료 시 복구 로직이 아직 없어 401이 나요(홈이 "다민" 폴백).
//        로그인 흐름 마무리하실 때 /auth/refresh 재발급으로 복구해주시면 될 것 같아요.
//        서버 엔드포인트(POST /api/v1/auth/refresh)는 배포돼 있는 것 확인했습니다(2026-07-22).
//   refreshToken 없음 = DevTokenProvider가 넣은 개발용 토큰 → 만료 시 자동 재로그인해 복구합니다.
//        (테스트 계정 경로가 며칠 켜둬도 만료를 못 느끼는 건 이것 때문 — 개발 편의용 의도된 동작입니다)
//
// (판별 근거: DevTokenProvider는 saveAccessToken만 저장, 실제 로그인은 saveTokens로 refreshToken까지 저장)
// TODO(로그인 연동 시): ①DevTokenProvider와 아래 개발용 분기 삭제,
//   ②실사용자 401 복구를 refreshToken 재발급(POST /api/v1/auth/refresh)으로 대체.
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
