package com.talkqquest.app.feature.mission.data

import com.talkqquest.app.core.network.ApiResponse
import com.talkqquest.app.feature.mission.data.model.ArchiveListResponse
import com.talkqquest.app.feature.mission.data.model.ConversationCreateRequest
import com.talkqquest.app.feature.mission.data.model.ConversationCreateResponse
import com.talkqquest.app.feature.mission.data.model.ConversationMessageRequest
import com.talkqquest.app.feature.mission.data.model.ConversationMessageResponse
import com.talkqquest.app.feature.mission.data.model.ConversationSuggestionsResponse
import com.talkqquest.app.feature.mission.data.model.CreateFeedbackRequest
import com.talkqquest.app.feature.mission.data.model.CreateFeedbackResponse
import com.talkqquest.app.feature.mission.data.model.CreatePhraseRequest
import com.talkqquest.app.feature.mission.data.model.CreatePhraseResponse
import com.talkqquest.app.feature.mission.data.model.DeleteArchiveItemResponse
import com.talkqquest.app.feature.mission.data.model.FeedbackDetailResponse
import com.talkqquest.app.feature.mission.data.model.MissionCompleteRequest
import com.talkqquest.app.feature.mission.data.model.MissionCompleteResponse
import com.talkqquest.app.feature.mission.data.model.MissionDetail
import com.talkqquest.app.feature.mission.data.model.MissionListItem
import com.talkqquest.app.feature.mission.data.model.MissionListResponse
import com.talkqquest.app.feature.mission.data.model.MissionPrepResponse
import com.talkqquest.app.feature.mission.data.model.MissionSaveResponse
import com.talkqquest.app.feature.mission.data.model.XpSummary
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

    // 미션 완료 처리 — 명세서 경로 그대로 서버 구현됨(2026-07-22 스웨거·실호출 확인).
    // body의 conversationId로 "어느 시도"인지 식별. XP 지급까지 서버가 처리(xpEarned 응답).
    // ★순서 주의: /conversations/{id}/finish를 먼저 부르면 이 호출이 거부됨("이미 종료 처리된 대화")
    //   — 성공 완료는 이 API 하나로 종료+완료가 같이 처리됨 (실측 확인).
    @POST("api/v1/missions/{missionId}/complete")
    suspend fun completeMission(
        @Path("missionId") missionId: String,
        @Body body: MissionCompleteRequest,
    ): ApiResponse<MissionCompleteResponse>

    // ── 대화(Conversation) — 2026-07-22 서버 배포 확인, 응답 실측 기준 ──

    // 대화 세션 생성. 인사말(첫 AI 발화)은 안 줌 — 화면 인트로는 로컬 문구 유지.
    @POST("api/v1/conversations")
    suspend fun createConversation(
        @Body body: ConversationCreateRequest,
    ): ApiResponse<ConversationCreateResponse>

    // 사용자 메시지 저장 + AI(guide) 응답 생성. 응답 = { userMessage, guideMessage }.
    @POST("api/v1/conversations/{conversationId}/messages")
    suspend fun sendConversationMessage(
        @Path("conversationId") conversationId: String,
        @Body body: ConversationMessageRequest,
    ): ApiResponse<ConversationMessageResponse>

    // 추천 답변(톡깨의 추천 답변 카드) 조회.
    @GET("api/v1/conversations/{conversationId}/suggestions")
    suspend fun getConversationSuggestions(
        @Path("conversationId") conversationId: String,
    ): ApiResponse<ConversationSuggestionsResponse>

    // XP/레벨 요약 — 완료 화면의 레벨업 연출용 (완료 전·후 조회).
    @GET("api/v1/xp/summary")
    suspend fun getXpSummary(): ApiResponse<XpSummary>

    // AI 피드백 생성 — dev 실계약(POST /feedback) 대조(2026-07-25). 대화 종료 후 이 대화의 피드백 생성 트리거.
    // 생성은 비동기(응답 status=pending) → 이후 getFeedbackDetail로 ready까지 폴링.
    @POST("api/v1/feedback")
    suspend fun createFeedback(
        @Body body: CreateFeedbackRequest,
    ): ApiResponse<CreateFeedbackResponse>

    // AI 피드백 상세 조회 — dev 실계약(GET /feedback/{feedbackId}) 대조(2026-07-25).
    // status=ready여야 점수/문구가 채워짐(pending이면 0/빈값). 응답에 미션 제목은 없음(topic만).
    @GET("api/v1/feedback/{feedbackId}")
    suspend fun getFeedbackDetail(
        @Path("feedbackId") feedbackId: String,
    ): ApiResponse<FeedbackDetailResponse>

    // 문장(베스트 문장) 저장 — 아카이브 '문장'에 저장. dev 백엔드 실계약 대조(2026-07-25).
    // 저장 항목 표시는 C(아카이브)지만, 저장 호출은 AI 피드백 상세(B)에서 발생.
    @POST("api/v1/archives/phrases")
    suspend fun savePhrase(
        @Body body: CreatePhraseRequest,
    ): ApiResponse<CreatePhraseResponse>

    // 문장 저장 해제 — DELETE /api/v1/archives/phrases/{phraseId}.
    // 시트에서 북마크를 끄면 호출 — 서버 보관함(문장)에서도 빠지게.
    @DELETE("api/v1/archives/phrases/{phraseId}")
    suspend fun deletePhrase(
        @Path("phraseId") phraseId: String,
    ): ApiResponse<DeleteArchiveItemResponse>

    // 저장한 문장 목록 — GET /api/v1/archives?type=phrase. 문장 저장 시트의 "최근 저장한 문장"에 표시.
    // 저장(위 savePhrase)과 같은 도메인이라 여기 함께 둔다.
    @GET("api/v1/archives")
    suspend fun getSavedPhrases(
        @Query("type") type: String = "phrase",
        @Query("sort") sort: String = "latest",
        @Query("size") size: Int = 5,
    ): ApiResponse<ArchiveListResponse>
}
