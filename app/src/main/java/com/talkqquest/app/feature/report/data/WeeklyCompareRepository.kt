package com.talkqquest.app.feature.report.data

import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.core.network.serverCall
import com.talkqquest.app.feature.report.data.model.CategoryRank
import com.talkqquest.app.feature.report.data.model.MetricChange
import com.talkqquest.app.feature.report.data.model.WeeklyCompareDetail
import com.talkqquest.app.feature.report.data.model.toGrowthReport
import com.talkqquest.app.feature.report.data.model.toWeeklyCompareReport
import javax.inject.Inject
import javax.inject.Singleton

// 주간 비교 리포트(홈/알림창에서 진입) 전용 Repository.
// ReportRepository(성장 리포트)와 파일을 나눠 둔다 — 모델·API는 공용이지만, 성장 리포트가
// 티어 개편으로 계속 바뀌는 중이라 한 파일에 같이 두면 서로 손대는 곳이 겹친다.
@Singleton
class WeeklyCompareRepository @Inject constructor(
    private val reportApi: ReportApi,
) {

    // 한 화면에 들어갈 값을 모아서 만든다.
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

    // 목업 = UI 14차 시안 값 그대로 (사용자 결정)
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
