package com.talkqquest.app.feature.report.data

import com.talkqquest.app.core.network.ApiResponse
import com.talkqquest.app.feature.report.data.model.DeleteReportResponse
import com.talkqquest.app.feature.report.data.model.GrowthReportResponse
import com.talkqquest.app.feature.report.data.model.ReportListResponse
import com.talkqquest.app.feature.report.data.model.SaveReportRequest
import com.talkqquest.app.feature.report.data.model.SaveReportResponse
import com.talkqquest.app.feature.report.data.model.WeeklyCompareResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// 리포트 API — dev 백엔드 실계약(report.controller.ts) 대조(2026-07-25). Bearer 필수(AuthInterceptor 자동).
// 목록/상세(GET /reports, /reports/{id})는 아카이브(보관함 리포트) 흐름 = C 담당이라 여기 없음.
interface ReportApi {

    // 성장 리포트 (성장 탭)
    @GET("api/v1/reports/growth")
    suspend fun getGrowth(): ApiResponse<GrowthReportResponse>

    // 주간 비교 리포트 (주간 비교 탭)
    @GET("api/v1/reports/weekly-compare")
    suspend fun getWeeklyCompare(): ApiResponse<WeeklyCompareResponse>

    // 리포트 저장 (리포트 저장 시트) — type: growth | weekly_compare
    @POST("api/v1/reports")
    suspend fun saveReport(
        @Body body: SaveReportRequest,
    ): ApiResponse<SaveReportResponse>

    // 리포트 저장 해제 — DELETE /api/v1/reports/{reportId} (dev #85, 2026-07-25 추가).
    // 시트에서 북마크를 끄면 호출 — 서버 보관함에서도 빠지게.
    @DELETE("api/v1/reports/{reportId}")
    suspend fun deleteReport(
        @Path("reportId") reportId: String,
    ): ApiResponse<DeleteReportResponse>

    // 저장한 리포트 목록 — GET /api/v1/reports. 리포트 저장 시트의 "최근 저장한 리포트"에 표시.
    // 보관함 목록(GET /archives?type=report)과 달리 항목마다 type(growth|weekly_compare)이 와서,
    // 북마크를 껐다 다시 켤 때 같은 종류로 정확히 재저장할 수 있다.
    @GET("api/v1/reports")
    suspend fun getSavedReports(): ApiResponse<ReportListResponse>
}
