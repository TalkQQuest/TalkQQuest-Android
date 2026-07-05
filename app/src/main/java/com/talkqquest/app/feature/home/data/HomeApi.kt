package com.talkqquest.app.feature.home.data

import com.talkqquest.app.core.network.ApiResponse
import com.talkqquest.app.feature.home.data.model.HomeSummary
import retrofit2.http.GET

// 홈 API (기능명세서 C101). Retrofit 인터페이스 예시.
// 각 기능은 이 패턴대로 자기 Api를 만들면 됩니다. 응답은 항상 ApiResponse<...> 로 감쌈.
interface HomeApi {

    @GET("api/v1/home/summary")
    suspend fun getHomeSummary(): ApiResponse<HomeSummary>
}
