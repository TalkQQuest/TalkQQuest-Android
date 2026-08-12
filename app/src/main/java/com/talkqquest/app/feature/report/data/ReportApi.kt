package com.talkqquest.app.feature.report.data

import com.talkqquest.app.core.network.ApiResponse
import com.talkqquest.app.feature.report.data.model.DeleteReportResponse
import com.talkqquest.app.feature.report.data.model.GrowthReportResponse
import com.talkqquest.app.feature.report.data.model.ReportListResponse
import com.talkqquest.app.feature.report.data.model.SaveReportRequest
import com.talkqquest.app.feature.report.data.model.SaveReportResponse
import com.talkqquest.app.feature.report.data.model.WeeklyCompareDetailResponse
import com.talkqquest.app.feature.report.data.model.WeeklyCompareListResponse
import com.talkqquest.app.feature.report.data.model.WeeklyCompareResponse
import com.talkqquest.app.feature.report.data.model.WeeklyCompareSaveResponse
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

    // ── 주간 비교 리포트(홈/알림창에서 진입) — 2026-08-10 백엔드 개편분 ──
    // 목록/상세로 분리됐다. "이번 주" 개념이 없어지고 가입일 기준으로 완전히 끝난 주끼리만 비교한다.
    // 실서버 실호출로 응답 확인(2026-08-11): 목록 3건(weekIndex 2·3·4), 상세에 metricChanges·highlights.

    // 목록 — 주차 이동에 쓴다. 최신이 앞.
    @GET("api/v1/reports/weekly-compare")
    suspend fun getWeeklyCompareList(): ApiResponse<WeeklyCompareListResponse>

    // 상세 — 지표 4축 변화·하이라이트. 목록의 id로 조회.
    @GET("api/v1/reports/weekly-compare/{reportId}")
    suspend fun getWeeklyCompareDetail(
        @Path("reportId") reportId: String,
    ): ApiResponse<WeeklyCompareDetailResponse>

    // 저장 / 저장 해제 — 성장 리포트와 달리 원본은 안 지워지고 isSaved만 바뀐다(백엔드 보고).
    @POST("api/v1/reports/weekly-compare/{reportId}/save")
    suspend fun saveWeeklyCompare(
        @Path("reportId") reportId: String,
    ): ApiResponse<WeeklyCompareSaveResponse>

    @DELETE("api/v1/reports/weekly-compare/{reportId}")
    suspend fun unsaveWeeklyCompare(
        @Path("reportId") reportId: String,
    ): ApiResponse<WeeklyCompareSaveResponse>

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
