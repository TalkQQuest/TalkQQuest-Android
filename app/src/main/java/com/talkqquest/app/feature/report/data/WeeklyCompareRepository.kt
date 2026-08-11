package com.talkqquest.app.feature.report.data

import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.core.network.serverCall
import com.talkqquest.app.feature.report.data.model.CategoryRank
import com.talkqquest.app.feature.report.data.model.MetricChange
import com.talkqquest.app.feature.report.data.model.WeeklyCompareDetail
import com.talkqquest.app.feature.report.data.model.WeeklyCompareListItem
import com.talkqquest.app.feature.report.data.model.toGrowthReport
import javax.inject.Inject
import javax.inject.Singleton

// 주간 비교 리포트(홈/알림창에서 진입) 전용 Repository.
// ReportRepository(성장 리포트)와 파일을 나눠 둔다 — 모델·API는 공용이지만, 성장 리포트가
// 티어 개편으로 계속 바뀌는 중이라 한 파일에 같이 두면 서로 손대는 곳이 겹친다.
//
// 서버 구조(2026-08-10 백엔드 개편, 2026-08-11 실호출 확인):
//   GET /reports/weekly-compare        → 목록 (주차 이동용). 최신이 앞
//   GET /reports/weekly-compare/{id}   → 상세 (지표 4축 변화·하이라이트)
// 화면에 필요한 「자주 연습한 주제」와 「미션 진행률」은 주간 응답에 없어 성장 리포트에서 가져온다.
@Singleton
class WeeklyCompareRepository @Inject constructor(
    private val reportApi: ReportApi,
) {

    // 주차 목록 — 화면 진입 시 한 번. 비면 목업 1건으로 폴백해 화면이 비지 않게 한다.
    suspend fun getWeekList(): ApiResult<List<WeeklyCompareListItem>> {
        val r = serverCall { reportApi.getWeeklyCompareList() }
        val list = (r as? ApiResult.Success)?.data?.reports.orEmpty()
        return ApiResult.Success(list)
    }

    // 선택한 주차의 상세 + 주제·진행률.
    // reportId가 비면(목록이 비어 실서버가 없을 때) 시안 목업을 그대로 돌려준다.
    suspend fun getWeekDetail(item: WeeklyCompareListItem?): ApiResult<WeeklyCompareDetail> {
        val fallback = stubWeeklyDetail
        if (item == null) return ApiResult.Success(fallback)

        val detail = serverCall { reportApi.getWeeklyCompareDetail(item.id) }
        val growth = serverCall { reportApi.getGrowth() }
        val d = (detail as? ApiResult.Success)?.data
        val g = (growth as? ApiResult.Success)?.data?.toGrowthReport()

        val metrics = d?.data?.metricChanges
            ?.map { MetricChange(name = it.label.ifBlank { it.key }, lastWeek = it.from, thisWeek = it.to) }
            ?.takeIf { it.isNotEmpty() }
            ?: fallback.metrics
        val highlights = d?.data?.highlights?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() }
            ?: fallback.highlights

        return ApiResult.Success(
            WeeklyCompareDetail(
                id = item.id,
                isSaved = d?.isSaved ?: item.isSaved,
                // TODO(백엔드 확인): 서버는 달력 주차를 안 준다. weekIndex(가입일 기준 N번째 주)뿐이라
                //   시안의 "7월 4주차 → 8월 1주차"를 그대로 만들 수 없다. 임시로 weekIndex를 쓴다.
                prevWeekLabel = weekLabel(item.weekIndex - 1),
                thisWeekLabel = weekLabel(item.weekIndex),
                metrics = metrics,
                highlights = highlights,
                topics = g?.categoryRanks?.takeIf { it.isNotEmpty() } ?: fallback.topics,
                completedMissions = g?.completedMissions ?: fallback.completedMissions,
                totalMissions = g?.totalMissions ?: fallback.totalMissions,
            ),
        )
    }

    // 저장 토글 — 성장 리포트의 삭제와 달리 원본은 안 지워지고 isSaved만 바뀐다(백엔드 보고).
    suspend fun setSaved(reportId: String, saved: Boolean): ApiResult<Boolean> {
        val r = if (saved) serverCall { reportApi.saveWeeklyCompare(reportId) }
        else serverCall { reportApi.unsaveWeeklyCompare(reportId) }
        return when (r) {
            is ApiResult.Success -> ApiResult.Success(r.data.isSaved)
            is ApiResult.Error -> r
            is ApiResult.Exception -> r
        }
    }

    private fun weekLabel(index: Int): String = "${index.coerceAtLeast(1)}주차"

    // 목업 = UI 14차 시안 값 그대로 (서버 실패/데모 시에만 쓰임)
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
