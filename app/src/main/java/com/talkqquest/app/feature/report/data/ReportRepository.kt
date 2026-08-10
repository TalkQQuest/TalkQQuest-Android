package com.talkqquest.app.feature.report.data

import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.core.network.serverCall
import com.talkqquest.app.core.util.toSavedDate
import com.talkqquest.app.feature.report.data.model.CategoryRank
import com.talkqquest.app.feature.report.data.model.Competency
import com.talkqquest.app.feature.report.data.model.CompetencyAxis
import com.talkqquest.app.feature.report.data.model.DeleteReportResponse
import com.talkqquest.app.feature.report.data.model.GrowthTierReport
import com.talkqquest.app.feature.report.data.model.HighlightItem
import com.talkqquest.app.feature.report.data.model.MetricChange
import com.talkqquest.app.feature.report.data.model.SaveReportRequest
import com.talkqquest.app.feature.report.data.model.SaveReportResponse
import com.talkqquest.app.feature.report.data.model.SavedReportItem
import com.talkqquest.app.feature.report.data.model.WeeklyCompareDetail
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

    // 성장 리포트(B) — 티어/핵심역량(growthTotals) 대개편 전이라 서버 매핑 없이 stub.
    // TODO(서버 growthTotals merge 후): GET /reports/growth 실 응답 → tier·별·4축 매핑으로 교체.
    suspend fun getGrowthReport(): ApiResult<GrowthTierReport> = ApiResult.Success(stubGrowth)

    // 주간 비교 리포트 — GET /api/v1/reports/weekly-compare.
    suspend fun getWeeklyCompare(): ApiResult<WeeklyCompareReport> {
        val r = serverCall { reportApi.getWeeklyCompare() }
        return if (r is ApiResult.Success) ApiResult.Success(r.data.toWeeklyCompareReport())
        else ApiResult.Success(stubWeekly)
    }

    // 주간 비교 리포트(홈/알림창에서 진입) 화면용 — 한 화면에 들어갈 값을 모아서 만든다.
    //  · 지표·하이라이트  ← GET /reports/weekly-compare
    //  · 주제·미션 진행률 ← GET /reports/growth (주간 응답에 없는 값이라 성장 쪽에서 가져옴)
    //  · 주차 라벨        ← 서버에 없음. 아래 stub 값 사용
    // TODO(서버 연동): 백엔드가 GET /reports/weekly-compare를 "완전히 끝난 주끼리 비교한 목록"으로
    //   바꾸고 상세를 별도 API로 분리했다(2026-08-10 보고). 새 스키마가 확정되면 주차 라벨을 서버에서
    //   받고, 목록을 그대로 받아 주차 이동에 쓰도록 이 메서드를 목록 반환으로 바꿀 것.
    suspend fun getWeeklyCompareDetail(): ApiResult<WeeklyCompareDetail> {
        val weekly = serverCall { reportApi.getWeeklyCompare() }
        val growth = serverCall { reportApi.getGrowth() }
        if (weekly !is ApiResult.Success && growth !is ApiResult.Success) {
            return ApiResult.Success(stubWeeklyDetail) // 둘 다 실패/데모 → 시안 목업
        }
        val w = (weekly as? ApiResult.Success)?.data?.toWeeklyCompareReport()
        val g = (growth as? ApiResult.Success)?.data?.toGrowthReport()
        return ApiResult.Success(
            WeeklyCompareDetail(
                prevWeekLabel = stubWeeklyDetail.prevWeekLabel,
                thisWeekLabel = stubWeeklyDetail.thisWeekLabel,
                metrics = w?.metrics?.takeIf { it.isNotEmpty() } ?: stubWeeklyDetail.metrics,
                highlights = w?.highlights?.map { (it.emphasis + it.rest).trim() }
                    ?.filter { it.isNotBlank() }
                    ?: stubWeeklyDetail.highlights,
                topics = g?.categoryRanks?.takeIf { it.isNotEmpty() } ?: stubWeeklyDetail.topics,
                completedMissions = g?.completedMissions ?: stubWeeklyDetail.completedMissions,
                totalMissions = g?.totalMissions ?: stubWeeklyDetail.totalMissions,
            ),
        )
    }

    // 리포트 저장 (리포트 저장 시트) — POST /api/v1/reports. type: "growth" | "weekly_compare".
    suspend fun saveReport(type: String): ApiResult<SaveReportResponse> =
        serverCall { reportApi.saveReport(SaveReportRequest(type = type)) }

    // 리포트 저장 해제 — DELETE /api/v1/reports/{reportId}.
    // 저장 응답에서 받은 서버 id로만 의미가 있고, 실패/데모면 조용히 무시(화면은 낙관적 표시 유지).
    suspend fun deleteReport(reportId: String): ApiResult<DeleteReportResponse> =
        serverCall { reportApi.deleteReport(reportId) }

    // 저장한 리포트 목록 (리포트 저장 시트의 "최근 저장한 리포트") — GET /api/v1/reports.
    // 서버가 페이지 파라미터를 안 받아 최신 size개만 잘라 쓴다(응답은 최신순 가정 — 아니어도 날짜로 정렬).
    // 실패/데모(USE_MOCK)면 Error를 그대로 돌려줘 호출부가 기존 목업을 유지하게 한다.
    suspend fun getSavedReports(size: Int = 5): ApiResult<List<SavedReportItem>> =
        when (val r = serverCall { reportApi.getSavedReports() }) {
            is ApiResult.Success -> ApiResult.Success(
                r.data.reports
                    .sortedByDescending { it.createdAt }
                    .take(size)
                    .map {
                        SavedReportItem(
                            id = it.id,
                            title = it.title,
                            savedDate = it.createdAt.toSavedDate(),
                            type = it.type,
                        )
                    },
            )
            is ApiResult.Error -> r
            is ApiResult.Exception -> r
        }

    // stub 값 = UI CSS 목업 그대로 (성장 티어 시스템 — growthTotals merge 전 기본값)
    private val stubGrowth = GrowthTierReport(
        tierName = "골드",
        tierStars = 2,
        nextStarsNeeded = 1,
        nextTierName = "플래티넘",
        competencies = listOf(
            Competency(CompetencyAxis.KINDNESS, "친절한 태도", "친절한 태도", score = 300, gain = 70),
            Competency(CompetencyAxis.INITIATIVE, "대화 주도", "대화 주도", score = 200, gain = 70),
            Competency(CompetencyAxis.EMPATHY, "공감 표현", "공감 능력", score = 100, gain = 70),
            Competency(CompetencyAxis.QUESTION_LINK, "질문 연결성", "질문 연결성", score = 300, gain = 70),
        ),
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

    // 주간 비교 리포트(홈/알림창에서 진입) 목업 — UI 14차 시안 값 그대로.
    private val stubWeeklyDetail = WeeklyCompareDetail(
        prevWeekLabel = "7월 4주차",
        thisWeekLabel = "8월 1주차",
        metrics = listOf(
            MetricChange(name = "친절한 태도", lastWeek = 240, thisWeek = 300),
            MetricChange(name = "대화 주도", lastWeek = 240, thisWeek = 300),
            MetricChange(name = "공감 표현", lastWeek = 320, thisWeek = 310),
            MetricChange(name = "질문 연결성", lastWeek = 280, thisWeek = 310),
        ),
        highlights = listOf(
            "질문 연결성을 꾸준히 개선하고 있어요",
            "친절한 태도가 가장 많이 상승되었어요",
        ),
        topics = listOf(
            CategoryRank(name = "여행", count = 10),
            CategoryRank(name = "음식", count = 9),
            CategoryRank(name = "일상", count = 7),
            CategoryRank(name = "인사", count = 4),
        ),
        completedMissions = 26,
        totalMissions = 100,
    )
}
