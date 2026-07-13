package com.talkqquest.app.feature.home.data

import com.talkqquest.app.core.network.ApiResponse
import com.talkqquest.app.feature.home.data.model.HomeSummary
import com.talkqquest.app.feature.home.data.model.UserMe
import retrofit2.http.GET

// 홈 API. 각 기능은 이 패턴대로 자기 Api를 만들면 됩니다. 응답은 항상 ApiResponse<...> 로 감쌈.
interface HomeApi {

    // 홈 요약 — 백엔드에 이 엔드포인트는 아직 없음. 지금은 getMe(실데이터) + stub 조합으로 대체.
    @GET("api/v1/home/summary")
    suspend fun getHomeSummary(): ApiResponse<HomeSummary>

    // 내 프로필 (서버 구현 완료) — 홈 인사말 닉네임·레벨·XP의 실데이터 소스
    @GET("api/v1/users/me")
    suspend fun getMe(): ApiResponse<UserMe>
}
