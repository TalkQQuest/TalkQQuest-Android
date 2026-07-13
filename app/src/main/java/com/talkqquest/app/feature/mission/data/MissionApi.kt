package com.talkqquest.app.feature.mission.data

import com.talkqquest.app.core.network.ApiResponse
import com.talkqquest.app.feature.mission.data.model.MissionCompleteRequest
import com.talkqquest.app.feature.mission.data.model.MissionCompleteResponse
import com.talkqquest.app.feature.mission.data.model.MissionDetail
import com.talkqquest.app.feature.mission.data.model.MissionListResponse
import com.talkqquest.app.feature.mission.data.model.MissionPrepResponse
import com.talkqquest.app.feature.mission.data.model.MissionSaveResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// 미션 API — 백엔드 dev 브랜치 mission.controller/mission-completion.controller 기준.
// 전 엔드포인트 Authorization: Bearer 필수 (AuthInterceptor가 자동 첨부).
interface MissionApi {

    // 미션 목록 및 필터 조회. 응답 data = { missions, pageInfo }
    @GET("api/v1/missions")
    suspend fun getMissions(
        @Query("difficulty") difficulty: String? = null,
        @Query("category") category: String? = null,
        @Query("saved") saved: Boolean? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
    ): ApiResponse<MissionListResponse>

    // 미션 상세 조회 (missionId = UUID 문자열)
    @GET("api/v1/missions/{missionId}")
    suspend fun getMissionDetail(
        @Path("missionId") missionId: String,
    ): ApiResponse<MissionDetail>

    // 대화 시작 준비 문장 조회 — 응답은 items[{type: question|starter|tip}] 구조
    @GET("api/v1/missions/{missionId}/prep")
    suspend fun getMissionPrep(
        @Path("missionId") missionId: String,
    ): ApiResponse<MissionPrepResponse>

    // 미션 저장(북마크)
    @POST("api/v1/missions/{missionId}/save")
    suspend fun saveMission(
        @Path("missionId") missionId: String,
    ): ApiResponse<MissionSaveResponse>

    // 미션 저장 취소
    @DELETE("api/v1/missions/{missionId}/save")
    suspend fun unsaveMission(
        @Path("missionId") missionId: String,
    ): ApiResponse<MissionSaveResponse>

    // 미션 완료 처리 — body: { conversationId, result, memo?, durationMinutes, emotion? }
    @POST("api/v1/missions/{missionId}/complete")
    suspend fun completeMission(
        @Path("missionId") missionId: String,
        @Body body: MissionCompleteRequest,
    ): ApiResponse<MissionCompleteResponse>
}
