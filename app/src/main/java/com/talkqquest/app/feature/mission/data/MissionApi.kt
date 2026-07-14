package com.talkqquest.app.feature.mission.data

import com.talkqquest.app.core.network.ApiResponse
import com.talkqquest.app.feature.mission.data.model.MissionCompleteRequest
import com.talkqquest.app.feature.mission.data.model.MissionCompleteResponse
import com.talkqquest.app.feature.mission.data.model.MissionDetail
import com.talkqquest.app.feature.mission.data.model.MissionListItem
import com.talkqquest.app.feature.mission.data.model.MissionListResponse
import com.talkqquest.app.feature.mission.data.model.MissionPrepResponse
import com.talkqquest.app.feature.mission.data.model.MissionSaveResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// 미션 API — 실서버 스웨거(https://talkqquest.shop/docs)와 대조해 확정.
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

    // 오늘의 추천 미션 (홈 카드). 응답 data = 미션 항목 1건. 미션이 없으면 404 MISSION_NOT_FOUND.
    @GET("api/v1/missions/today")
    suspend fun getTodayMission(): ApiResponse<MissionListItem>

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

    // 미션 완료 처리.
    // ★경로 주의: 기능명세서엔 /missions/{missionId}/complete로 적혀 있으나,
    //   실서버는 대화(conversations) 밑에 conversationId 기준으로 구현돼 있음(스웨거 확인).
    //   같은 미션을 여러 번 할 수 있어 "어느 시도인지"는 conversationId만 식별 가능.
    //   body에도 conversationId가 또 들어감(서버 규격 그대로).
    @POST("api/v1/conversations/{conversationId}/complete")
    suspend fun completeMission(
        @Path("conversationId") conversationId: String,
        @Body body: MissionCompleteRequest,
    ): ApiResponse<MissionCompleteResponse>
}
