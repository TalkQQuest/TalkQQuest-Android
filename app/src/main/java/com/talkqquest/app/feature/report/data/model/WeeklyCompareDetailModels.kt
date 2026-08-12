package com.talkqquest.app.feature.report.data.model

import kotlinx.serialization.Serializable

// ── 서버 DTO — 2026-08-11 실서버 실호출로 응답 확인(추정 아님) ──

// GET /api/v1/reports/weekly-compare — 목록. 주차 이동에 쓴다(최신이 앞).
@Serializable
data class WeeklyCompareListResponse(
    val reports: List<WeeklyCompareListItem> = emptyList(),
)

@Serializable
data class WeeklyCompareListItem(
    val id: String = "",
    val weekIndex: Int = 0,          // 가입일 기준 N번째 주 (달력 주차가 아님)
    val isSaved: Boolean = false,
    val createdAt: String = "",
    val overallScoreChange: OverallScoreChangeDto = OverallScoreChangeDto(),
)

// GET /api/v1/reports/weekly-compare/{reportId} — 상세
@Serializable
data class WeeklyCompareDetailResponse(
    val id: String = "",
    val weekIndex: Int = 0,
    val isSaved: Boolean = false,
    val createdAt: String = "",
    // 좌우 주차 이동용 이웃 리포트 id (백엔드 추가 2026-08-11). 그 방향에 리포트가 없으면 null.
    // previousReportId = 더 예전 주차(왼쪽), nextReportId = 더 최근 주차(오른쪽).
    // 실서버 3건으로 확인: weekIndex 4의 previous가 3, 2의 previous가 null.
    val previousReportId: String? = null,
    val nextReportId: String? = null,
    val data: WeeklyCompareDetailData = WeeklyCompareDetailData(),
)

@Serializable
data class WeeklyCompareDetailData(
    val lastWeek: WeeklyActivityDto = WeeklyActivityDto(),
    val thisWeek: WeeklyActivityDto = WeeklyActivityDto(),
    val xpChangeRate: Double = 0.0,
    val overallScoreChange: OverallScoreChangeDto = OverallScoreChangeDto(),
    val metricChanges: List<WeeklyMetricChangeDto> = emptyList(),
    val highlights: List<String> = emptyList(),
)

// POST .../save, DELETE .../{id} — 저장 토글. 성장 리포트와 달리 원본은 안 지워진다.
@Serializable
data class WeeklyCompareSaveResponse(
    val id: String = "",
    val isSaved: Boolean = false,
)

// ── 주간 비교 리포트(홈/알림창에서 진입) 화면 모델 ──
// UI 14차 "주간 비교 리포트(홈/알림창에서 진입)" 프레임 전사 기준.
// ReportScreen 안의 '주간 비교' 탭과 달리 주차 이동·자주 연습한 주제·미션 진행률이 함께 있는
// 독립 화면이라, 탭이 쓰는 WeeklyCompareReport와는 별도 모델로 둔다.
data class WeeklyCompareDetail(
    val id: String = "",                 // 서버 리포트 id — 저장 토글에 씀
    val isSaved: Boolean = false,        // 보관함에 저장돼 있는지
    // 좌우 화살표가 데려갈 리포트 id. null이면 그 방향엔 더 볼 리포트가 없다.
    // 목록 인덱스가 아니라 서버가 상세에 직접 실어 주는 값을 쓴다 — 목록이 잘려 와도 이동이 맞는다.
    val prevReportId: String? = null,
    val nextReportId: String? = null,
    val prevWeekLabel: String,           // 왼쪽 주차 ("7월 4주차")
    val thisWeekLabel: String,           // 오른쪽 주차 ("8월 1주차")
    val metrics: List<MetricChange>,     // 핵심 지표 4종 (친절한 태도/대화 주도/공감 표현/질문 연결성)
    val highlights: List<String>,        // 개선 하이라이트 — 서버가 통문장으로 준다
    val topics: List<CategoryRank>,      // 자주 연습한 주제 (많이 한 순)
    val completedMissions: Int,          // 완료한 미션 수
    val totalMissions: Int,              // 전체 미션 수
) {
    // 도넛 진행률 (0~100). 전체가 0이면 0%로 — 나눗셈 방어.
    val progressPercent: Int
        get() = if (totalMissions <= 0) 0 else (completedMissions * 100) / totalMissions
}
