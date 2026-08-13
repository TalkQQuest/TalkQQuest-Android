package com.talkqquest.app.feature.report.data

import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.core.network.serverCall
import com.talkqquest.app.feature.report.data.model.CategoryRank
import com.talkqquest.app.feature.report.data.model.MetricChange
import com.talkqquest.app.feature.report.data.model.WeeklyCompareDetail
import com.talkqquest.app.feature.report.data.model.WeeklyCompareListItem
import javax.inject.Inject
import javax.inject.Singleton

// 주간 비교 리포트(홈/알림창에서 진입) 전용 Repository.
// ReportRepository(성장 리포트)와 파일을 나눠 둔다 — 모델·API는 공용이지만, 성장 리포트가
// 티어 개편으로 계속 바뀌는 중이라 한 파일에 같이 두면 서로 손대는 곳이 겹친다.
//
// 서버 구조(2026-08-10 백엔드 개편, 2026-08-11 실호출 확인):
//   GET /reports/weekly-compare        → 목록 (주차 이동용). 최신이 앞
//   GET /reports/weekly-compare/{id}   → 상세 (주차 문구·지표 4축 변화·하이라이트·주제·미션 진행률)
// 2026-08-13 백엔드 반영으로 「자주 연습한 주제」·「미션 진행률」·주차 문구가 상세에 들어왔다.
// 그 전에는 성장 리포트(GET /reports/growth)를 한 번 더 불러 채웠는데, 이제 호출하지 않는다.
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

    // 한 리포트의 상세 + 주제·진행률.
    // reportId가 비면(목록이 비어 실서버가 없을 때) 시안 목업을 그대로 돌려준다.
    // 좌우 이동도 이 함수 하나로 한다 — 서버가 상세에 이웃 리포트 id를 실어 주기 때문에
    // 목록에서 몇 번째인지 셀 필요가 없다.
    suspend fun getWeekDetail(reportId: String?): ApiResult<WeeklyCompareDetail> {
        val fallback = stubWeeklyDetail
        if (reportId.isNullOrBlank()) return ApiResult.Success(fallback)

        val detail = serverCall { reportApi.getWeeklyCompareDetail(reportId) }
        val d = (detail as? ApiResult.Success)?.data

        // 상세를 못 받으면 주차 라벨도 이웃 id도 없어 좌우 이동이 통째로 막힌다.
        // 반쪽짜리로 그리지 말고 다른 실패 경로와 똑같이 시안 목업으로 떨어뜨린다.
        if (d == null) return ApiResult.Success(fallback)

        val metrics = d.data.metricChanges
            .map { MetricChange(name = it.label.ifBlank { it.key }, lastWeek = it.from, thisWeek = it.to) }
            .takeIf { it.isNotEmpty() }
            ?: fallback.metrics
        val highlights = d.data.highlights.filter { it.isNotBlank() }.takeIf { it.isNotEmpty() }
            ?: fallback.highlights

        // 서버가 "7월 4주차 → 8월 1주차" 한 문장으로 준다. 화면은 화살표 좌우로 나눠 그리므로 쪼갠다.
        // 형식이 달라지면 쪼개기가 실패하므로 그때는 weekIndex로 만든 예전 라벨을 쓴다.
        val period = d.periodLabel?.split("→")?.map { it.trim() }?.takeIf { it.size == 2 }
        val topics = d.data.topCategories
            .map { CategoryRank(name = it.category, count = it.count) }
            .takeIf { it.isNotEmpty() } ?: fallback.topics

        return ApiResult.Success(
            WeeklyCompareDetail(
                id = d.id.ifBlank { reportId },
                isSaved = d.isSaved,
                prevReportId = d.previousReportId,
                nextReportId = d.nextReportId,
                prevWeekLabel = period?.get(0) ?: weekLabel(d.weekIndex - 1),
                thisWeekLabel = period?.get(1) ?: weekLabel(d.weekIndex),
                metrics = metrics,
                highlights = highlights,
                topics = topics,
                completedMissions = d.data.missionProgress.completed,
                // 전체 미션 수가 0이면 진행률이 0%로 고정돼 도넛이 죽는다 — 그때만 목업 값으로 받친다.
                totalMissions = d.data.missionProgress.total.takeIf { it > 0 } ?: fallback.totalMissions,
            ),
        )
    }

    // 저장 토글 — 성장 리포트의 삭제와 달리 원본은 안 지워지고 isSaved만 바뀐다(백엔드 보고).
    suspend fun setSaved(reportId: String, saved: Boolean): ApiResult<Boolean> {
        val r = if (saved) serverCall { reportApi.saveWeeklyCompare(reportId) }
        else serverCall { reportApi.unsaveWeeklyCompare(reportId) }
        return when (r) {
            // 저장 응답과 해제 응답의 필드 구성이 다르므로 HTTP 성공 여부로 요청 상태를 확정한다.
            is ApiResult.Success -> ApiResult.Success(saved)
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
