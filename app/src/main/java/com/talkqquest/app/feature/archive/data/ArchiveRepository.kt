package com.talkqquest.app.feature.archive.data

import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.archive.data.model.ArchiveConversationDetailResponse
import com.talkqquest.app.feature.archive.data.model.ArchiveConversationFeedbackDto
import com.talkqquest.app.feature.archive.data.model.ArchiveConversationMessageDto
import com.talkqquest.app.feature.archive.data.model.ArchivePhraseDetailResponse
import com.talkqquest.app.feature.archive.data.model.ArchiveRecentActivity
import com.talkqquest.app.feature.archive.data.model.ArchiveSearchItem
import com.talkqquest.app.feature.archive.data.model.ArchiveSearchResponse
import com.talkqquest.app.feature.archive.data.model.ArchiveSummary
import com.talkqquest.app.feature.archive.data.model.ConversationDetailMock
import com.talkqquest.app.feature.archive.data.model.MissionSaveResponse
import com.talkqquest.app.feature.archive.data.model.PageInfo
import com.talkqquest.app.feature.archive.data.model.ReviewChatMessage
import com.talkqquest.app.feature.archive.data.model.SavePhraseRequest

import com.talkqquest.app.feature.archive.data.model.Competency
import com.talkqquest.app.feature.archive.data.model.CompetencyAxis
import com.talkqquest.app.feature.archive.data.model.GrowthReport
import com.talkqquest.app.feature.archive.data.model.HighlightItem
import com.talkqquest.app.feature.archive.data.model.MetricChange
import com.talkqquest.app.feature.archive.data.model.WeeklyCompareReport
import com.talkqquest.app.feature.archive.data.model.WeeklyCompareReportUiModel
import com.talkqquest.app.feature.archive.data.model.CategoryRank

import com.talkqquest.app.feature.archive.ui.ArchiveMissionItem
import com.talkqquest.app.feature.archive.ui.BookmarkArchiveItem
import com.talkqquest.app.feature.archive.viewmodel.ActivityType
import com.talkqquest.app.feature.archive.viewmodel.RecentActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArchiveRepository @Inject constructor(
    private val archiveApi: ArchiveApi
) {
    val isMockMode = false

    private val stubMissions = mutableListOf(
        ArchiveMissionItem("1", "학교 생활 꿀팁 나누기", "일상 대화", "보통", 8, 30, isCompleted = true, isSaved = true, completedDate = "2026.08.10"),
        ArchiveMissionItem("2", "최근 본 영화 이야기하기", "짧은 대화", "쉬움", 5, 20, isCompleted = false, isSaved = true, completedDate = "2026.08.09"),
        ArchiveMissionItem("3", "처음 보는 사람에게 짧게 인사하기", "짧은 대화", "쉬움", 2, 20, isCompleted = true, isSaved = true, completedDate = "2026.08.08")
    )

    private val stubConversations = mutableListOf(
        RecentActivity(id = "1", title = "대학교 전공과 진로에 대해 이야기 했어요", type = ActivityType.CONVERSATION, status = "대화 완료", date = "2026.08.10", duration = "12:34", tags = listOf("학교", "진로"), summary = "서로의 전공과 관심 분야를 공유하고, 진로 고민에 대해 조언을 주고받았어요."),
        RecentActivity(id = "2", title = "여행 계획을 공유하며 추천 장소를 주고 받았어요", type = ActivityType.CONVERSATION, status = "대화 완료", date = "2026.08.09", duration = "05:15", tags = listOf("여행", "취미"), summary = "이번 주말 여행 계획을 이야기하고, 가고 싶은 장소를 추천했어요."),
        RecentActivity(id = "3", title = "처음 보는 사람에게 짧게 인사하기", type = ActivityType.CONVERSATION, status = "대화 완료", date = "2026.08.08", duration = "02:40", tags = emptyList(), summary = null)
    )

    private val stubSentences = mutableListOf(
        BookmarkArchiveItem(id = "1", title = "네, 안녕하세요. 항상 아메리카노만 마셨는데, 오늘은 좀 달달한 걸 먹고 싶어요.", status = "문장 저장", date = "2026.08.10", isSaved = true, memoKeywords = listOf("상황극", "요청하기", "정중함"), memoText = "자신의 평소 취향과 현재 원하는 바를 명확하고 정중하게 전달하는 표현입니다.", relatedConversationId = "1")
    )

    private val stubReports = mutableListOf(
        BookmarkArchiveItem(id = "report_g1", title = "처음 보는 사람에게 짧게 인사하기", status = "성장 리포트", date = "2026.08.10", isSaved = true),
        BookmarkArchiveItem(id = "report_g2", title = "오랜만에 만난 친구와 안부 나누기", status = "성장 리포트", date = "2026.08.09", isSaved = true),
        BookmarkArchiveItem(id = "report_g3", title = "카페에서 자연스럽게 주문하기", status = "성장 리포트", date = "2026.08.08", isSaved = true),
        BookmarkArchiveItem(id = "report_w1", title = "8월 1-2주차 주간 비교 리포트", status = "주간 비교 리포트", date = "2026.08.10", isSaved = true),
        BookmarkArchiveItem(id = "report_w2", title = "7월 4주-8월 1주차 주간 비교 리포트", status = "주간 비교 리포트", date = "2026.08.06", isSaved = true),
        BookmarkArchiveItem(id = "report_w3", title = "7월 3주-4주차 주간 비교 리포트", status = "주간 비교 리포트", date = "2026.07.29", isSaved = true)
    )

    private val stubReportDetails = mapOf(
        "report_g1" to Pair<GrowthReport?, WeeklyCompareReport?>(
            GrowthReport("플래티넘", 2, 1, "다이아", listOf(
                Competency(CompetencyAxis.KINDNESS, "친절한 태도", "친절한 태도", 300, 280, 85),
                Competency(CompetencyAxis.INITIATIVE, "대화 주도", "대화 주도", 300, 250, 70),
                Competency(CompetencyAxis.EMPATHY, "공감 표현", "공감 능력", 300, 210, 92),
                Competency(CompetencyAxis.QUESTION_LINK, "질문 연결성", "질문 연결성", 300, 290, 88)
            )), null
        ),
        "report_g2" to Pair<GrowthReport?, WeeklyCompareReport?>(
            GrowthReport("골드", 1, 2, "플래티넘", listOf(
                Competency(CompetencyAxis.KINDNESS, "친절한 태도", "친절한 태도", 300, 150, 60),
                Competency(CompetencyAxis.INITIATIVE, "대화 주도", "대화 주도", 300, 280, 80),
                Competency(CompetencyAxis.EMPATHY, "공감 표현", "공감 능력", 300, 120, 50),
                Competency(CompetencyAxis.QUESTION_LINK, "질문 연결성", "질문 연결성", 300, 200, 75)
            )), null
        )
    )

    private val stubWeeklyCompareDetails = mapOf(
        "report_w1" to WeeklyCompareReportUiModel(
            id = "report_w1",
            title = "4주차 주간 비교 리포트",
            isSaved = true,
            report = WeeklyCompareReport(
                metrics = listOf(
                    MetricChange("친절한 태도", 88, 92), MetricChange("대화 주도", 86, 88),
                    MetricChange("공감 표현", 82, 85), MetricChange("질문 연결성", 74, 78)
                ),
                highlights = listOf(HighlightItem("친절한 태도가", " 가장 많이 상승되었어요")),
                completedMissions = 4, totalMissions = 0, topCategories = emptyList()
            ),
            prevReportId = "report_w2",
            nextReportId = null
        ),
        "report_w2" to WeeklyCompareReportUiModel(
            id = "report_w2",
            title = "3주차 주간 비교 리포트",
            isSaved = true,
            report = WeeklyCompareReport(
                metrics = listOf(
                    MetricChange("친절한 태도", 80, 88), MetricChange("대화 주도", 82, 86),
                    MetricChange("공감 표현", 78, 82), MetricChange("질문 연결성", 70, 74)
                ),
                highlights = listOf(HighlightItem("전체 점수가", " 꾸준히 상승 중이에요")),
                completedMissions = 3, totalMissions = 0, topCategories = emptyList()
            ),
            prevReportId = "report_w3",
            nextReportId = "report_w1"
        )
    )

    private val stubConversationDetails = listOf(
        ConversationDetailMock(
            id = "1", title = "처음 보는 사람에게 짧게 인사하기", date = "2026.08.10", duration = "05:30", summaryKeywords = listOf("자기 성장"),
            summaryText = "대화를 시작하는 연습을 진행했습니다.", mainContentText = "먼저 인사를 건네며 대화를 시작했어요.",
            feedbacks = listOf("친절한 태도" to 92),
            messages = listOf(ReviewChatMessage("1", "안녕하세요!", false, "9:20"))
        )
    )

    suspend fun toggleMissionBookmark(id: String, isCurrentlySaved: Boolean): ApiResult<MissionSaveResponse> {
        if (isMockMode) {
            val index = stubMissions.indexOfFirst { it.id == id }
            var savedStatus = false
            if (index != -1) {
                stubMissions[index] = stubMissions[index].copy(isSaved = !stubMissions[index].isSaved)
                savedStatus = stubMissions[index].isSaved
            }
            return ApiResult.Success(MissionSaveResponse(missionId = id, isSaved = savedStatus, savedAt = "2026.08.10"))
        } else {
            return try {
                val response = if (isCurrentlySaved) archiveApi.deleteMissionArchive(id) else archiveApi.saveMissionArchive(id)
                if (response.data != null) ApiResult.Success(response.data) else ApiResult.Error(null, response.message ?: "오류가 발생했습니다.")
            } catch (e: Exception) { ApiResult.Exception(e) }
        }
    }

    suspend fun toggleSentenceBookmark(
        id: String,
        isCurrentlySaved: Boolean,
        conversationId: String? = null,
        content: String? = null,
        memo: String? = null
    ): ApiResult<Any> {
        if (isMockMode) {
            val index = stubSentences.indexOfFirst { it.id == id }
            if (index != -1) stubSentences[index] = stubSentences[index].copy(isSaved = !stubSentences[index].isSaved)
            return ApiResult.Success(Unit)
        } else {
            return try {
                if (isCurrentlySaved) {
                    val response = archiveApi.deletePhraseArchive(id)
                    if (response.data != null) ApiResult.Success(response.data) else ApiResult.Error(null, response.message ?: "오류가 발생했습니다.")
                } else {
                    val response = archiveApi.savePhraseArchive(
                        SavePhraseRequest(
                            conversationId = conversationId ?: "",
                            content = content ?: "",
                            memo = memo
                        )
                    )
                    if (response.data != null) ApiResult.Success(response.data) else ApiResult.Error(null, response.message ?: "오류가 발생했습니다.")
                }
            } catch (e: Exception) { ApiResult.Exception(e) }
        }
    }

    suspend fun toggleReportBookmark(id: String, isCurrentlySaved: Boolean): ApiResult<Any> {
        if (isMockMode) {
            val index = stubReports.indexOfFirst { it.id == id }
            if (index != -1) stubReports[index] = stubReports[index].copy(isSaved = !stubReports[index].isSaved)
            return ApiResult.Success(Unit)
        } else {
            return try {
                if (isCurrentlySaved) {
                    val response = archiveApi.deleteReportArchive(id)
                    if (response.data != null) ApiResult.Success(response.data) else ApiResult.Error(null, response.message ?: "해제 실패")
                } else {
                    ApiResult.Error(null, "리포트는 해제만 가능합니다.")
                }
            } catch (e: Exception) {
                ApiResult.Exception(e)
            }
        }
    }

    fun getArchiveMissions(): List<ArchiveMissionItem> = stubMissions.toList()
    fun getArchiveConversations(): List<RecentActivity> = stubConversations.toList()
    fun getArchiveSentences(): List<BookmarkArchiveItem> = stubSentences.toList()
    fun getArchiveReports(): List<BookmarkArchiveItem> = stubReports.toList()

    suspend fun getSavedSentenceDetail(id: String): ApiResult<ArchivePhraseDetailResponse> {
        if (isMockMode) {
            val sentence = stubSentences.find { it.id == id }
            if (sentence != null) {
                val relatedConversation = stubConversations.find { it.id == sentence.relatedConversationId }
                val mockResponse = ArchivePhraseDetailResponse(
                    id = sentence.id,
                    content = sentence.title,
                    memo = sentence.memoText,
                    missionTitle = relatedConversation?.title,
                    conversationId = sentence.relatedConversationId,
                    folderId = null,
                    duration = relatedConversation?.duration,
                    description = relatedConversation?.summary,
                    summaryChips = sentence.memoKeywords,
                    createdAt = sentence.date
                )
                return ApiResult.Success(mockResponse)
            } else return ApiResult.Error(null, "문장을 찾을 수 없습니다.")
        } else {
            return try {
                val response = archiveApi.getPhraseDetail(id)
                if (response.data != null) ApiResult.Success(response.data) else ApiResult.Error(null, response.message ?: "오류 발생")
            } catch (e: Exception) { ApiResult.Exception(e) }
        }
    }

    // --- 💡 기존의 성장 리포트 전용 로직 ---
    suspend fun getArchiveReportDetail(id: String): ApiResult<Triple<String, GrowthReport?, WeeklyCompareReport?>> {
        if (isMockMode) {
            val title = stubReports.find { it.id == id }?.title ?: "성장 리포트"
            val reports = stubReportDetails[id]
            return if (reports != null) {
                ApiResult.Success(Triple(title, reports.first, reports.second))
            } else {
                ApiResult.Error(null, "리포트를 불러오지 못했습니다.")
            }
        } else {
            return try {
                val response = archiveApi.getReportDetail(id)
                val data = response.data
                if (data != null) {
                    val growth = data.growth?.let { g ->
                        val totals = g.growthTotals
                        val kTotal = totals?.kindnessTotal ?: 0
                        val iTotal = totals?.initiativeTotal ?: 0
                        val eTotal = totals?.empathyTotal ?: 0
                        val qTotal = totals?.questionLinkTotal ?: 0

                        val completedRhombuses = minOf(kTotal, iTotal, eTotal, qTotal) / 300

                        val tiers = listOf("브론즈", "실버", "골드", "플래티넘", "다이아", "마스터")
                        val tierIndex = (completedRhombuses / 3).coerceIn(0, 5)

                        val tierName = tiers[tierIndex]
                        val tierStars = if (tierIndex == 5) 0 else completedRhombuses % 3
                        val nextTierName = if (tierIndex < 5) tiers[tierIndex + 1] else "마스터"
                        val nextStarsNeeded = if (tierIndex < 5) 3 - tierStars else 0

                        val baseScore = completedRhombuses * 300
                        val kScore = minOf(kTotal - baseScore, 300)
                        val iScore = minOf(iTotal - baseScore, 300)
                        val eScore = minOf(eTotal - baseScore, 300)
                        val qScore = minOf(qTotal - baseScore, 300)

                        val recent = g.recentScores
                        val competencies = listOf(
                            Competency(CompetencyAxis.KINDNESS, "친절한 태도", "친절한 태도", 300, kScore, recent?.kindness ?: 0),
                            Competency(CompetencyAxis.INITIATIVE, "대화 주도", "대화 주도", 300, iScore, recent?.initiative ?: 0),
                            Competency(CompetencyAxis.EMPATHY, "공감 표현", "공감 능력", 300, eScore, recent?.empathy ?: 0),
                            Competency(CompetencyAxis.QUESTION_LINK, "질문 연결성", "질문 연결성", 300, qScore, recent?.questionLink ?: 0)
                        )

                        GrowthReport(
                            tierName = tierName,
                            tierStars = tierStars,
                            nextStarsNeeded = nextStarsNeeded,
                            nextTierName = nextTierName,
                            competencies = competencies
                        )
                    } ?: GrowthReport("브론즈", 0, 3, "실버", emptyList<Competency>())

                    val displayTitle = data.title ?: data.period ?: "톡깨 리포트"

                    ApiResult.Success(Triple(displayTitle, growth, null))
                } else {
                    ApiResult.Error(null, response.message ?: "오류가 발생했습니다.")
                }
            } catch (e: Exception) {
                ApiResult.Exception(e)
            }
        }
    }

    // --- 💡 신규 주간 비교 리포트 전용 로직 ---
    suspend fun getWeeklyCompareReportDetail(id: String): ApiResult<WeeklyCompareReportUiModel> {
        if (isMockMode) {
            val report = stubWeeklyCompareDetails[id]
            return if (report != null) ApiResult.Success(report)
            else ApiResult.Error(null, "리포트를 불러오지 못했습니다.")
        } else {
            return try {
                val response = archiveApi.getWeeklyCompareReportDetail(id)
                val data = response.data
                if (data != null) {
                    val report = WeeklyCompareReport(
                        metrics = data.data.metricChanges.map {
                            MetricChange(name = it.label.ifBlank { it.key }, lastWeek = it.from.toInt(), thisWeek = it.to.toInt())
                        },
                        highlights = data.data.highlights.map {
                            HighlightItem(emphasis = "", rest = it)
                        },
                        completedMissions = data.data.thisWeek.completedMissionCount,
                        totalMissions = 0, // 💡 새로운 API 명세에 해당 필드가 사라져 기본값(0) 할당
                        topCategories = emptyList() // 💡 새로운 API 명세에 해당 필드가 사라져 기본값(empty) 할당
                    )

                    val title = "${data.weekIndex}주차 주간 비교 리포트"

                    ApiResult.Success(
                        WeeklyCompareReportUiModel(
                            id = data.id,
                            title = title,
                            isSaved = data.isSaved,
                            report = report,
                            prevReportId = data.previousReportId,
                            nextReportId = data.nextReportId
                        )
                    )
                } else {
                    ApiResult.Error(null, response.message ?: "오류 발생")
                }
            } catch (e: Exception) {
                ApiResult.Exception(e)
            }
        }
    }

    suspend fun getConversationDetail(id: String): ApiResult<ArchiveConversationDetailResponse> {
        if (isMockMode) {
            val mockDetail = stubConversationDetails.find { it.id == id }
            if (mockDetail != null) {
                val mappedResponse = ArchiveConversationDetailResponse(
                    conversationId = mockDetail.id,
                    missionTitle = mockDetail.title,
                    summary = mockDetail.summaryText,
                    duration = mockDetail.duration,
                    summaryChips = mockDetail.summaryKeywords,
                    messages = mockDetail.messages.map { ArchiveConversationMessageDto(sender = if (it.isFromUser) "USER" else "BOT", content = it.text, sentAt = it.time) },
                    feedback = ArchiveConversationFeedbackDto(feedbackId = "mock_feedback", kindnessScore = mockDetail.feedbacks.find { it.first == "친절한 태도" }?.second ?: 0, initiativeScore = mockDetail.feedbacks.find { it.first == "대화 주도" }?.second ?: 0, empathyScore = mockDetail.feedbacks.find { it.first == "공감 능력" }?.second ?: 0, questionLinkScore = mockDetail.feedbacks.find { it.first == "질문 연결성" }?.second ?: 0)
                )
                return ApiResult.Success(mappedResponse)
            } else return ApiResult.Error(null, "대화 상세 정보를 찾을 수 없습니다.")
        } else {
            return try {
                val response = archiveApi.getConversationDetail(id)
                if (response.data != null) ApiResult.Success(response.data) else ApiResult.Error(null, response.message ?: "오류가 발생했습니다.")
            } catch (e: Exception) { ApiResult.Exception(e) }
        }
    }

    suspend fun getArchiveSummary(): ApiResult<ArchiveSummary> {
        if (isMockMode) {
            val allMockActivities = mutableListOf<ArchiveRecentActivity>()

            stubMissions.filter { it.isCompleted && it.isSaved }.forEach { allMockActivities.add(ArchiveRecentActivity(id = it.id, referenceId = it.id, type = "MISSION", title = it.title, isBookmarked = it.isSaved, missionStatus = "COMPLETED", category = it.category, difficulty = it.difficulty, estimatedMinutes = it.duration, rewardXp = it.xp, createdAt = it.completedDate)) }
            stubConversations.forEach { allMockActivities.add(ArchiveRecentActivity(id = it.id, referenceId = it.id, type = "CONVERSATION", title = it.title, isBookmarked = false, createdAt = it.date, duration = it.duration, tags = it.tags, description = it.summary)) }
            stubSentences.filter { it.isSaved }.forEach { allMockActivities.add(ArchiveRecentActivity(id = it.id, referenceId = it.id, type = "PHRASE", title = it.title, isBookmarked = true, createdAt = it.date)) }
            stubReports.filter { it.isSaved }.forEach { allMockActivities.add(ArchiveRecentActivity(id = it.id, referenceId = it.id, type = "REPORT", reportType = "GROWTH", title = it.title, isBookmarked = true, createdAt = it.date)) }

            val summary = ArchiveSummary(totalCount = allMockActivities.size, missionRecordCount = stubMissions.count { it.isSaved }, conversationCount = stubConversations.size, phraseCount = stubSentences.count { it.isSaved }, reportCount = stubReports.count { it.isSaved }, recentItems = allMockActivities.sortedByDescending { it.createdAt }.take(4))
            return ApiResult.Success(summary)
        } else {
            return try {
                val response = archiveApi.getArchiveSummary()
                if (response.data != null) ApiResult.Success(response.data) else ApiResult.Error(null, response.message ?: "오류가 발생했습니다.")
            } catch (e: Exception) { ApiResult.Exception(e) }
        }
    }

    suspend fun searchArchives(
        keyword: String? = null, type: String? = null, startDate: String? = null,
        endDate: String? = null, sort: String? = null, missionFilter: String? = null, folderId: String? = null,
        tag: String? = null, page: Int? = null, size: Int? = null
    ): ApiResult<ArchiveSearchResponse> {
        if (isMockMode) {
            val allMockItems = mutableListOf<ArchiveSearchItem>()
            val query = keyword?.trim() ?: ""

            val filteredMissions = stubMissions.filter { query.isEmpty() || it.title.contains(query, true) }
            filteredMissions.forEach {
                allMockItems.add(
                    ArchiveSearchItem(
                        id = it.id, referenceId = it.id, archiveItemId = it.id,
                        type = "MISSION", title = it.title, isBookmarked = it.isSaved,
                        missionStatus = if (it.isCompleted) "COMPLETED" else "IN_PROGRESS",
                        category = it.category, difficulty = it.difficulty,
                        estimatedMinutes = it.duration, rewardXp = it.xp,
                        missionId = it.id, missionRecordId = it.id, createdAt = it.completedDate ?: "2026.08.10"
                    )
                )
            }

            val filteredConvos = stubConversations.filter { query.isEmpty() || it.title.contains(query, true) }
            filteredConvos.forEach {
                allMockItems.add(
                    ArchiveSearchItem(
                        id = it.id, referenceId = it.id, archiveItemId = it.id,
                        type = "CONVERSATION", title = it.title, isBookmarked = false,
                        tags = it.tags, description = it.summary, duration = it.duration, createdAt = it.date
                    )
                )
            }

            val filteredPhrases = stubSentences.filter { query.isEmpty() || it.title.contains(query, true) }
            filteredPhrases.forEach {
                allMockItems.add(
                    ArchiveSearchItem(
                        id = it.id, referenceId = it.id, archiveItemId = it.id,
                        type = "PHRASE", title = it.title, isBookmarked = it.isSaved, createdAt = it.date
                    )
                )
            }

            val filteredReports = stubReports.filter { query.isEmpty() || it.title.contains(query, true) }
            filteredReports.forEach {
                val rType = if (it.title.contains("주간 비교")) "WEEKLY_COMPARE" else "GROWTH"
                allMockItems.add(
                    ArchiveSearchItem(
                        id = it.id, referenceId = it.id, archiveItemId = it.id,
                        type = "REPORT", reportType = rType, title = it.title,
                        isBookmarked = it.isSaved, createdAt = it.date
                    )
                )
            }

            val reqType = type?.uppercase()
            val finalItems = if (reqType.isNullOrBlank() || reqType == "전체" || reqType == "ALL") {
                allMockItems
            } else {
                val filterType = if (reqType == "SENTENCE") "PHRASE" else reqType
                allMockItems.filter { it.type == filterType }
            }

            return ApiResult.Success(ArchiveSearchResponse(totalCount = finalItems.size, items = finalItems, pageInfo = PageInfo(finalItems.size, 1, 0)))
        } else {
            return try {
                val response = archiveApi.searchArchives(keyword, type, startDate, endDate, sort, missionFilter, folderId, tag, page, size)
                if (response.data != null) ApiResult.Success(response.data) else ApiResult.Error(null, response.message ?: "오류가 발생했습니다.")
            } catch (e: Exception) { ApiResult.Exception(e) }
        }
    }
}