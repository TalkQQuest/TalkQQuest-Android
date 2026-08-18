package com.talkqquest.app.feature.archive.data.model

import kotlinx.serialization.Serializable

// --- 대화 상세 조회 DTO ---
@Serializable
data class ArchiveConversationDetailResponse(
    val conversationId: String,
    val missionTitle: String? = null,
    val description: String? = null, // 💡 추가됨: 상단 대화 카드용 짧은 요약
    val summary: String? = null,     // 기존: 중간 '대화 요약' 줄글
    val duration: String? = null,
    val summaryChips: List<String> = emptyList(),
    val keyPoints: List<String> = emptyList(), // 💡 추가됨: 하단 '주요 내용' 불릿 리스트
    val messages: List<ArchiveConversationMessageDto> = emptyList(),
    val feedback: ArchiveConversationFeedbackDto? = null
)

@Serializable
data class ArchiveConversationMessageDto(
    val id: String? = null,
    val role: String? = null,
    val sender: String? = null,
    val content: String,
    val createdAt: String? = null,
    val sentAt: String? = null
) {
    val actualRole: String get() = role ?: sender ?: "bot"
    val actualTime: String get() = createdAt ?: sentAt ?: ""
}

@Serializable
data class ArchiveConversationFeedbackDto(
    val id: String? = null,
    val feedbackId: String? = null,
    val kindnessScore: Int = 0,
    val initiativeScore: Int = 0,
    val empathyScore: Int = 0,
    val questionLinkScore: Int = 0
) {
    val actualId: String get() = id ?: feedbackId ?: ""
}

// --- 저장 문장 상세 조회 DTO ---
@Serializable
data class ArchivePhraseDetailResponse(
    val id: String,
    val content: String,
    val memo: String? = null,
    val missionTitle: String? = null,
    val conversationId: String? = null,
    val folderId: String? = null,
    val duration: String? = null,
    val description: String? = null,
    val summaryChips: List<String> = emptyList(),
    val createdAt: String
)

// --- 성장 리포트 상세 조회 응답 DTO ---
@Serializable
data class ArchiveReportDetailResponse(
    val id: String,
    val period: String? = null,
    val title: String? = null,
    val growth: ReportGrowthDto? = null,
    val recentScores: ReportMetricsDto? = null,
    val createdAt: String
)

@Serializable
data class ReportGrowthDto(
    val levelBefore: Int,
    val levelAfter: Int,
    val weeklyTrend: List<ReportWeeklyTrendDto>,
    val trendChangeRate: Double,
    val topCategories: List<ReportTopCategoryDto>,
    val missionProgress: ReportMissionProgressDto,
    val growthTotals: ReportGrowthTotalsDto? = null
)

@Serializable
data class ReportGrowthTotalsDto(
    val kindnessTotal: Int = 0,
    val initiativeTotal: Int = 0,
    val empathyTotal: Int = 0,
    val questionLinkTotal: Int = 0
)

@Serializable
data class ReportWeeklyTrendDto(
    val week: String,
    val score: Int
)

// 성장 리포트와 주간 비교 리포트가 같은 모양으로 받는다. 주간 비교 쪽은 응답에 없을 수도 있어
// 기본값을 둔다(없으면 주제 칩이 비고 진행률 0 — 화면이 죽지는 않는다).
@Serializable
data class ReportTopCategoryDto(
    val category: String = "",
    val count: Int = 0
)

@Serializable
data class ReportMissionProgressDto(
    val completed: Int = 0,
    val total: Int = 0
)

// --- 주간 비교 리포트 전용 상세 조회 응답 DTO (신규) ---
@Serializable
data class WeeklyCompareReportDetailResponse(
    val id: String,
    val weekIndex: Int,
    val isSaved: Boolean,
    val data: WeeklyCompareDataDto,
    val createdAt: String,
    val previousReportId: String? = null,
    val nextReportId: String? = null,
    // 완성된 주차 문구 (백엔드 추가 2026-08-13). 실측 "7월 4주차 → 8월 1주차".
    // weekIndex는 가입일 기준 N번째 주라 달력 주차를 만들 수 없으므로 이 값을 그대로 쓴다.
    val periodLabel: String? = null
)

@Serializable
data class WeeklyCompareDataDto(
    val thisWeek: ReportWeeklyDataDto,
    val lastWeek: ReportWeeklyDataDto,
    val xpChangeRate: Double,
    val overallScoreChange: ReportScoreChangeDto,
    val metricChanges: List<ReportMetricChangeDto>,
    val highlights: List<String>,
    // 자주 연습한 주제 · 미션 진행률 (백엔드 추가 2026-08-13).
    // 선언이 없어 파싱에서 버려지는 바람에 주제 칩이 비고 진행률이 0으로 나왔다.
    val topCategories: List<ReportTopCategoryDto> = emptyList(),
    val missionProgress: ReportMissionProgressDto = ReportMissionProgressDto()
)

@Serializable
data class ReportWeeklyDataDto(
    val completedMissionCount: Int,
    val xpEarned: Int,
    val metrics: ReportMetricsDto
)

@Serializable
data class ReportMetricsDto(
    val kindness: Int,
    val initiative: Int,
    val empathy: Int,
    val questionLink: Int
)

@Serializable
data class ReportScoreChangeDto(
    val from: Double,
    val to: Double,
    val delta: Double
)

@Serializable
data class ReportMetricChangeDto(
    val key: String,
    val label: String,
    val from: Double,
    val to: Double,
    val delta: Double
)

// --- 아카이브 검색 및 필터 응답 DTO ---
@Serializable
data class ArchiveSearchResponse(
    val totalCount: Int = 0,
    val items: List<ArchiveSearchItem> = emptyList(),
    val pageInfo: PageInfo? = null
)

@Serializable
data class PageInfo(
    val totalCount: Int = 0,
    val totalPages: Int = 0,
    val currentPage: Int = 0
)

@Serializable
data class ArchiveSearchItem(
    val archiveItemId: String? = null,
    val referenceId: String? = null,
    val id: String,
    val type: String,
    val reportType: String? = null,
    val title: String,
    val tags: List<String> = emptyList(),
    val description: String? = null,
    val duration: String? = null,
    val folderId: String? = null,
    val isBookmarked: Boolean = false,
    val missionStatus: String? = null,
    val category: String? = null,
    val difficulty: String? = null,
    val estimatedMinutes: Int? = null,
    val rewardXp: Int? = null,
    val missionId: String? = null,
    val missionRecordId: String? = null,
    val createdAt: String
)

// --- 아카이브 홈 요약(F101) DTO ---
@Serializable
data class ArchiveSummary(
    val totalCount: Int = 0,
    val missionRecordCount: Int = 0,
    val conversationCount: Int = 0,
    val phraseCount: Int = 0,
    val reportCount: Int = 0,
    val recentItems: List<ArchiveRecentActivity> = emptyList()
)

@Serializable
data class ArchiveRecentActivity(
    val id: String,
    val referenceId: String? = null,
    val type: String,
    val reportType: String? = null,
    val title: String,
    val tags: List<String> = emptyList(),
    val description: String? = null,
    val duration: String? = null,
    val isBookmarked: Boolean = false,
    val missionId: String? = null,
    val conversationId: String? = null,
    val missionRecordId: String? = null,
    val missionStatus: String? = null,
    val category: String? = null,
    val difficulty: String? = null,
    val estimatedMinutes: Int? = null,
    val rewardXp: Int? = null,
    val createdAt: String
)

@Serializable
data class MissionSaveResponse(
    val missionId: String,
    val isSaved: Boolean,
    val savedAt: String? = null
)

// --- 문장 저장(POST) 요청/응답 DTO ---
@Serializable
data class SavePhraseRequest(
    val conversationId: String,
    val content: String,
    val memo: String? = null
)

@Serializable
data class SavePhraseResponse(
    val id: String,
    val conversationId: String,
    val content: String,
    val memo: String? = null,
    val createdAt: String
)

// --- 문장 저장 해제(DELETE) 응답 DTO ---
@Serializable
data class DeletePhraseResponse(
    val itemId: String,
    val deleted: Boolean
)

// --- 리포트 저장/해제 관련 DTO ---
@Serializable
data class SaveReportRequest(
    val type: String = "growth"
)

@Serializable
data class SaveReportResponse(
    val reportId: String,
    val type: String,
    val period: String,
    val createdAt: String
)

@Serializable
data class DeleteReportResponse(
    val reportId: String,
    val deleted: Boolean
)

data class ReviewChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val time: String
)

data class ConversationDetailMock(
    val id: String,
    val title: String,
    val date: String,
    val duration: String,
    val summaryKeywords: List<String>,
    val summaryText: String,
    val mainContentText: String,
    val feedbacks: List<Pair<String, Int>>,
    val messages: List<ReviewChatMessage>
)

data class MetricChange(
    val name: String,
    val lastWeek: Int,
    val thisWeek: Int
)

data class HighlightItem(
    val emphasis: String,
    val rest: String
)

data class CategoryRank(
    val name: String,
    val count: Int
)

data class WeeklyCompareReport(
    val metrics: List<MetricChange>,
    val highlights: List<HighlightItem>,
    val completedMissions: Int = 0,
    val totalMissions: Int = 0,
    val topCategories: List<CategoryRank> = emptyList()
)

// 💡 뷰모델에서 사용하기 편하게 묶어주는 UI 래퍼 클래스
data class WeeklyCompareReportUiModel(
    val id: String,
    val title: String,
    val isSaved: Boolean,
    val report: WeeklyCompareReport,
    val prevReportId: String?,
    val nextReportId: String?,
    // 주차 이동 줄에 그대로 쓰는 좌·우 라벨. 서버 periodLabel("7월 4주차 → 8월 1주차")을 갈라 담는다.
    // 예전에는 화면이 제목 문자열을 정규식으로 되짚어 뽑았는데, 제목 형식이 달라지면
    // "이전 주차 / 선택 주차"라는 기본 문구가 그대로 노출됐다.
    val prevWeekLabel: String = "",
    val thisWeekLabel: String = ""
)