package com.talkqquest.app.feature.archive.data.model

import kotlinx.serialization.Serializable

// --- 대화 상세 조회 DTO ---
@Serializable
data class ArchiveConversationDetailResponse(
    val conversationId: String,
    val missionTitle: String? = null,
    val summary: String? = null,
    val durationMinutes: Int? = null,
    val summaryChips: List<String> = emptyList(),
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
    val summaryChips: List<String> = emptyList(),
    val createdAt: String
)

// --- 리포트 상세 조회 응답 DTO ---
@Serializable
data class ArchiveReportDetailResponse(
    val id: String,
    val period: String? = null,
    val weeklyComparePeriod: String? = null,
    val title: String? = null,
    val growth: ReportGrowthDto? = null,
    val weeklyCompare: ReportWeeklyCompareDto? = null,
    val createdAt: String
)

@Serializable
data class ReportGrowthDto(
    val levelBefore: Int,
    val levelAfter: Int,
    val weeklyTrend: List<ReportWeeklyTrendDto>,
    val trendChangeRate: Double,
    val topCategories: List<ReportTopCategoryDto>,
    val missionProgress: ReportMissionProgressDto
)

@Serializable
data class ReportWeeklyTrendDto(
    val week: String,
    val score: Int
)

@Serializable
data class ReportTopCategoryDto(
    val category: String,
    val count: Int
)

@Serializable
data class ReportMissionProgressDto(
    val completed: Int,
    val total: Int
)

@Serializable
data class ReportWeeklyCompareDto(
    val thisWeek: ReportWeeklyDataDto,
    val lastWeek: ReportWeeklyDataDto,
    val xpChangeRate: Double,
    val overallScoreChange: ReportScoreChangeDto,
    val metricChanges: List<ReportMetricChangeDto>,
    val highlights: List<String>
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
    val from: Int,
    val to: Int,
    val delta: Int
)

@Serializable
data class ReportMetricChangeDto(
    val key: String,
    val label: String,
    val from: Int,
    val to: Int,
    val delta: Int
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
    val duration: String? = null, // 💡 추가됨: 대화 소요 시간
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
    val duration: String? = null, // 💡 추가됨: 대화 소요 시간
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

// ==========================================
// 💡 아카이브 전용 리포트 UI 데이터 모델
// ==========================================
enum class CompetencyAxis { KINDNESS, INITIATIVE, EMPATHY, QUESTION_LINK }

data class Competency(
    val axis: CompetencyAxis,
    val label: String,
    val legendLabel: String,
    val maxScore: Int,
    val score: Int,
    val gain: Int = score
)

data class GrowthReport(
    val tierName: String,
    val tierStars: Int,
    val nextStarsNeeded: Int,
    val nextTierName: String,
    val competencies: List<Competency>
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