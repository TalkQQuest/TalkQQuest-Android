package com.talkqquest.app.feature.report.data

import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.core.network.serverCall
import com.talkqquest.app.core.util.toSavedDate
import com.talkqquest.app.feature.report.data.model.CategoryRank
import com.talkqquest.app.feature.report.data.model.DeleteReportResponse
import com.talkqquest.app.feature.report.data.model.GrowthReport
import com.talkqquest.app.feature.report.data.model.HighlightItem
import com.talkqquest.app.feature.report.data.model.MetricChange
import com.talkqquest.app.feature.report.data.model.SaveReportRequest
import com.talkqquest.app.feature.report.data.model.SaveReportResponse
import com.talkqquest.app.feature.report.data.model.SavedReportItem
import com.talkqquest.app.feature.report.data.model.WeeklyCompareReport
import com.talkqquest.app.feature.report.data.model.toGrowthReport
import com.talkqquest.app.feature.report.data.model.toWeeklyCompareReport
import javax.inject.Inject
import javax.inject.Singleton

// 리포트 Repository (미션/홈 패턴과 동일한 계층).
// 서버 우선(GET /reports/*) + 실패/데모(USE_MOCK)면 목업 폴백 (미션·알림과 동일 구조).
@Singleton
class ReportRepository @Inject constructor(
    private val reportApi: ReportApi,
) {

    // 성장 리포트 — GET /api/v1/reports/growth (dev 실계약 대조 2026-07-25).
    suspend fun getGrowthReport(): ApiResult<GrowthReport> {
        val r = serverCall { reportApi.getGrowth() }
        return if (r is ApiResult.Success) ApiResult.Success(r.data.toGrowthReport())
        else ApiResult.Success(stubGrowth)
    }

    // 주간 비교 리포트 — GET /api/v1/reports/weekly-compare.
    suspend fun getWeeklyCompare(): ApiResult<WeeklyCompareReport> {
        val r = serverCall { reportApi.getWeeklyCompare() }
        return if (r is ApiResult.Success) ApiResult.Success(r.data.toWeeklyCompareReport())
        else ApiResult.Success(stubWeekly)
    }

    // 리포트 저장 (리포트 저장 시트) — POST /api/v1/reports. type: "growth" | "weekly_compare".
    suspend fun saveReport(type: String): ApiResult<SaveReportResponse> =
        serverCall { reportApi.saveReport(SaveReportRequest(type = type)) }

    // 리포트 저장 해제 — DELETE /api/v1/reports/{reportId}.
    // 저장 응답에서 받은 서버 id로만 의미가 있고, 실패/데모면 조용히 무시(화면은 낙관적 표시 유지).
    suspend fun deleteReport(reportId: String): ApiResult<DeleteReportResponse> =
        serverCall { reportApi.deleteReport(reportId) }

    // 저장한 리포트 목록 (리포트 저장 시트의 "최근 저장한 리포트") — GET /archives?type=report.
    // 실패/데모(USE_MOCK)면 Error를 그대로 돌려줘 호출부가 기존 목업을 유지하게 한다.
    suspend fun getSavedReports(size: Int = 5): ApiResult<List<SavedReportItem>> =
        when (val r = serverCall { reportApi.getSavedReports(size = size) }) {
            is ApiResult.Success -> ApiResult.Success(
                r.data.items.map {
                    SavedReportItem(
                        id = it.referenceId ?: it.id,
                        title = it.title,
                        savedDate = it.createdAt.toSavedDate(),
                        isSaved = it.isBookmarked,
                    )
                },
            )
            is ApiResult.Error -> r
            is ApiResult.Exception -> r
        }

    // stub 값 = UI CSS 목업 그대로 (사용자 결정)
    private val stubGrowth = GrowthReport(
        prevLevel = 1,
        currentLevel = 2,
        growthPercent = 18,
        weekLabels = listOf("7월 4주", "8월 1주", "8월 2주", "8월 3주"),
        categoryRanks = listOf(
            CategoryRank(name = "여행", count = 10),
            CategoryRank(name = "음식", count = 9),
            CategoryRank(name = "일상", count = 7),
            CategoryRank(name = "인사", count = 4),
        ),
        completedMissions = 26,
        totalMissions = 100,
    )

    private val stubWeekly = WeeklyCompareReport(
        metrics = listOf(
            MetricChange(name = "친절한 태도", lastWeek = 88, thisWeek = 92),
            MetricChange(name = "대화 주도", lastWeek = 86, thisWeek = 88),
            MetricChange(name = "공감 표현", lastWeek = 82, thisWeek = 85),
            MetricChange(name = "질문 연결성", lastWeek = 74, thisWeek = 78),
        ),
        highlights = listOf(
            HighlightItem(emphasis = "전체 점수", rest = "가 78점에서 86점으로 상승했어요"),
            HighlightItem(emphasis = "친절한 태도", rest = "가 가장 많이 상승되었어요"),
            HighlightItem(emphasis = "질문 연결성", rest = "을 꾸준히 개선하고 있어요"),
        ),
    )
}
