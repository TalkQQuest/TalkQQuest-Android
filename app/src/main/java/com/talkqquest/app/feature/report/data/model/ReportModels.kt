package com.talkqquest.app.feature.report.data.model

import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

// 리포트 화면 모델 (명세 E102 — 응답 필드가 명세에 없어 화면(UI CSS) 기준으로 정의.
// TODO(서버 연동): GET /api/v1/reports/monthly · weekly-compare 응답 확정되면 필드 맞춤)

// 성장 리포트 탭
data class GrowthReport(
    val prevLevel: Int,                     // 저번 주 레벨
    val currentLevel: Int,                  // 이번 주 레벨
    val growthPercent: Int,                 // 대화 성장 추이 우측 "+ 18%"
    val weekLabels: List<String>,           // 차트 x축 라벨 ("7월 4주" …)
    val categoryRanks: List<CategoryRank>,  // 카테고리 TOP (많이 한 순)
    val completedMissions: Int,             // 완료한 미션 수
    val totalMissions: Int,                 // 전체 미션 수
)

data class CategoryRank(
    val name: String,  // 카테고리 이름 (여행/음식/…)
    val count: Int,    // 완료 횟수
)

// 주간 비교 리포트 탭
data class WeeklyCompareReport(
    val metrics: List<MetricChange>,     // 핵심 지표 4종 (E101 항목 순서 고정)
    val highlights: List<HighlightItem>, // 개선 하이라이트 문구
)

// 하이라이트 문구: 앞 키워드만 보라 강조 (피그마 렌더 — 나머지는 검정).
// TODO(서버 연동): 응답이 통문장으로 오면 키워드 분리 방식 협의해 조정.
data class HighlightItem(
    val emphasis: String,  // 보라 강조 부분 ("전체 점수" 등 — 조사 제외)
    val rest: String,      // 나머지 문장
)

data class MetricChange(
    val name: String,   // 지표 이름 (친절한 태도/대화 주도/…)
    val lastWeek: Int,  // 지난주 점수
    val thisWeek: Int,  // 이번 주 점수
)

// ── 서버 DTO — dev 백엔드 report.dto.ts 대조(2026-07-25) ──
// GET /api/v1/reports/growth
@Serializable
data class GrowthReportResponse(
    val levelBefore: Int = 0,
    val levelAfter: Int = 0,
    val weeklyTrend: List<WeeklyTrendPoint> = emptyList(),
    val trendChangeRate: Double = 0.0, // 성장 추이 우측 "+N%" (rate라 소수 가능 → 표시 시 반올림)
    val topCategories: List<TopCategoryDto> = emptyList(),
    val missionProgress: MissionProgressDto = MissionProgressDto(),
)

@Serializable
data class WeeklyTrendPoint(val week: String = "", val score: Int = 0)

@Serializable
data class TopCategoryDto(val category: String = "", val count: Int = 0)

@Serializable
data class MissionProgressDto(val completed: Int = 0, val total: Int = 0)

// GET /api/v1/reports/weekly-compare
@Serializable
data class WeeklyCompareResponse(
    val thisWeek: WeeklyActivityDto = WeeklyActivityDto(),
    val lastWeek: WeeklyActivityDto = WeeklyActivityDto(),
    val xpChangeRate: Double = 0.0,
    val overallScoreChange: OverallScoreChangeDto = OverallScoreChangeDto(),
    val metricChanges: List<WeeklyMetricChangeDto> = emptyList(),
    val highlights: List<String> = emptyList(),
)

@Serializable
data class WeeklyActivityDto(
    val completedMissionCount: Int = 0,
    val xpEarned: Int = 0,
    val metrics: WeeklyMetricsDto = WeeklyMetricsDto(),
)

@Serializable
data class WeeklyMetricsDto(
    val kindness: Int = 0,
    val initiative: Int = 0,
    val empathy: Int = 0,
    val questionLink: Int = 0,
)

@Serializable
data class OverallScoreChangeDto(val from: Int = 0, val to: Int = 0, val delta: Int = 0)

@Serializable
data class WeeklyMetricChangeDto(
    val key: String = "",
    val label: String = "",
    val from: Int = 0,
    val to: Int = 0,
    val delta: Int = 0,
)

// POST /api/v1/reports (리포트 저장 시트) — type: growth | weekly_compare
@Serializable
data class SaveReportRequest(val type: String)

@Serializable
data class SaveReportResponse(
    val reportId: String = "",
    val type: String = "",
    val period: String = "",
    val createdAt: String = "",
)

// DELETE /api/v1/reports/{reportId} (저장 해제)
@Serializable
data class DeleteReportResponse(
    val reportId: String = "",
    val deleted: Boolean = false,
)

// ── 매퍼: 서버 → 화면 모델 ──
fun GrowthReportResponse.toGrowthReport() = GrowthReport(
    prevLevel = levelBefore,
    currentLevel = levelAfter,
    growthPercent = trendChangeRate.roundToInt(),
    weekLabels = weeklyTrend.map { it.week },
    categoryRanks = topCategories.map { CategoryRank(name = it.category, count = it.count) },
    completedMissions = missionProgress.completed,
    totalMissions = missionProgress.total,
)

fun WeeklyCompareResponse.toWeeklyCompareReport() = WeeklyCompareReport(
    metrics = metricChanges.map {
        MetricChange(name = it.label.ifBlank { it.key }, lastWeek = it.from, thisWeek = it.to)
    },
    // TODO(협의): 서버가 통문장으로 줘서 앞 키워드 보라 강조 분리를 못 함 — 전부 rest로(검정). 분리 규칙 정해지면 조정.
    highlights = highlights.map { HighlightItem(emphasis = "", rest = it) },
)
