package com.talkqquest.app.feature.report.data

import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.report.data.model.CategoryRank
import com.talkqquest.app.feature.report.data.model.GrowthReport
import com.talkqquest.app.feature.report.data.model.HighlightItem
import com.talkqquest.app.feature.report.data.model.MetricChange
import com.talkqquest.app.feature.report.data.model.WeeklyCompareReport
import javax.inject.Inject
import javax.inject.Singleton

// 리포트 Repository (미션/홈 패턴과 동일한 계층).
@Singleton
class ReportRepository @Inject constructor() {

    // TODO(서버 연동 전 임시): GET /api/v1/reports/monthly 붙으면 stub 지우고
    //     return safeApiCall { reportApi.getMonthlyReport(month) } 로 교체.
    suspend fun getGrowthReport(): ApiResult<GrowthReport> = ApiResult.Success(stubGrowth)

    // TODO(서버 연동 전 임시): GET /api/v1/reports/weekly-compare 붙으면 위와 동일하게 교체.
    suspend fun getWeeklyCompare(): ApiResult<WeeklyCompareReport> = ApiResult.Success(stubWeekly)

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
