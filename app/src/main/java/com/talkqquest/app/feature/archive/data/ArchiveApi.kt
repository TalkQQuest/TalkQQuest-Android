package com.talkqquest.app.feature.archive.data

import com.talkqquest.app.core.network.ApiResponse
import com.talkqquest.app.feature.archive.data.model.ArchiveConversationDetailResponse
import com.talkqquest.app.feature.archive.data.model.ArchivePhraseDetailResponse
import com.talkqquest.app.feature.archive.data.model.ArchiveReportDetailResponse
import com.talkqquest.app.feature.archive.data.model.ArchiveSearchResponse
import com.talkqquest.app.feature.archive.data.model.ArchiveSummary
import com.talkqquest.app.feature.archive.data.model.DeletePhraseResponse
import com.talkqquest.app.feature.archive.data.model.DeleteReportResponse
import com.talkqquest.app.feature.archive.data.model.MissionSaveResponse
import com.talkqquest.app.feature.archive.data.model.SavePhraseRequest
import com.talkqquest.app.feature.archive.data.model.SavePhraseResponse
import com.talkqquest.app.feature.archive.data.model.SaveReportRequest
import com.talkqquest.app.feature.archive.data.model.SaveReportResponse
import com.talkqquest.app.feature.archive.data.model.WeeklyCompareReportDetailResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ArchiveApi {
    @GET("api/v1/archives/summary")
    suspend fun getArchiveSummary(): ApiResponse<ArchiveSummary>

    @GET("api/v1/archives")
    suspend fun searchArchives(
        @Query("keyword") keyword: String? = null,
        @Query("type") type: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("sort") sort: String? = null,
        @Query("missionFilter") missionFilter: String? = null,
        @Query("folderId") folderId: String? = null,
        @Query("tag") tag: String? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): ApiResponse<ArchiveSearchResponse>

    @GET("api/v1/archives/conversations/{conversationId}")
    suspend fun getConversationDetail(
        @Path("conversationId") conversationId: String
    ): ApiResponse<ArchiveConversationDetailResponse>

    @GET("api/v1/archives/phrases/{phraseId}")
    suspend fun getPhraseDetail(
        @Path("phraseId") phraseId: String
    ): ApiResponse<ArchivePhraseDetailResponse>

    // 💡 기존의 공용 리포트 엔드포인트는 이제 성장 리포트 전용으로 사용
    @GET("api/v1/reports/{reportId}")
    suspend fun getReportDetail(
        @Path("reportId") reportId: String
    ): ApiResponse<ArchiveReportDetailResponse>

    // 💡 신규 주간 비교 리포트 전용 상세 조회 API
    @GET("api/v1/reports/weekly-compare/{weeklyCompareReportId}")
    suspend fun getWeeklyCompareReportDetail(
        @Path("weeklyCompareReportId") weeklyCompareReportId: String
    ): ApiResponse<WeeklyCompareReportDetailResponse>

    @POST("api/v1/missions/{missionId}/save")
    suspend fun saveMissionArchive(
        @Path("missionId") missionId: String
    ): ApiResponse<MissionSaveResponse>

    @DELETE("api/v1/missions/{missionId}/save")
    suspend fun deleteMissionArchive(
        @Path("missionId") missionId: String
    ): ApiResponse<MissionSaveResponse>

    @POST("api/v1/archives/phrases")
    suspend fun savePhraseArchive(
        @Body request: SavePhraseRequest
    ): ApiResponse<SavePhraseResponse>

    @DELETE("api/v1/archives/phrases/{phraseId}")
    suspend fun deletePhraseArchive(
        @Path("phraseId") phraseId: String
    ): ApiResponse<DeletePhraseResponse>

    @POST("api/v1/reports")
    suspend fun saveReportArchive(
        @Body request: SaveReportRequest
    ): ApiResponse<SaveReportResponse>

    @DELETE("api/v1/reports/{reportId}")
    suspend fun deleteReportArchive(
        @Path("reportId") reportId: String
    ): ApiResponse<DeleteReportResponse>
}