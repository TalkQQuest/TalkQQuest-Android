package com.talkqquest.app.feature.mission.data

import com.talkqquest.app.core.network.ApiResponse
import com.talkqquest.app.feature.mission.data.model.MissionListItem
import retrofit2.http.GET
import retrofit2.http.Query

// 미션 API (기능명세서 C102). 응답은 항상 ApiResponse<...> 로 감쌈.
interface MissionApi {

    // 미션 목록 및 필터 조회. Query: difficulty, category, saved, page, size (명세서 C102)
    // 페이징(page/size)은 백엔드 확정 후 추가.
    @GET("api/v1/missions")
    suspend fun getMissions(
        @Query("difficulty") difficulty: String? = null,
        @Query("category") category: String? = null,
    ): ApiResponse<List<MissionListItem>>
}
