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
import com.talkqquest.app.feature.archive.data.model.SaveReportRequest
import com.talkqquest.app.feature.archive.ui.ArchiveMissionItem
import com.talkqquest.app.feature.archive.ui.BookmarkArchiveItem
import com.talkqquest.app.feature.archive.viewmodel.ActivityType
import com.talkqquest.app.feature.archive.viewmodel.RecentActivity
import com.talkqquest.app.feature.report.data.model.CategoryRank
import com.talkqquest.app.feature.report.data.model.GrowthReport
import com.talkqquest.app.feature.report.data.model.HighlightItem
import com.talkqquest.app.feature.report.data.model.MetricChange
import com.talkqquest.app.feature.report.data.model.WeeklyCompareReport
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class ArchiveRepository @Inject constructor(
    private val archiveApi: ArchiveApi
) {
    val isMockMode = false

    private val stubMissions = mutableListOf(
        ArchiveMissionItem("1", "학교 생활 꿀팁 나누기", "일상 대화", "보통", 8, 30, isCompleted = true, isSaved = true, completedDate = "2026.08.20"),
        ArchiveMissionItem("2", "최근 본 영화 이야기하기", "짧은 대화", "쉬움", 5, 20, isCompleted = false, isSaved = true, completedDate = "2026.08.20"),
        ArchiveMissionItem("3", "처음 보는 사람에게 짧게 인사하기", "짧은 대화", "쉬움", 2, 20, isCompleted = true, isSaved = true, completedDate = "2026.08.19")
    )

    private val stubConversations = mutableListOf(
        // 💡 API 명세 변경 반영: tags, summary 파라미터 매핑을 위해 Mock 데이터에 반영
        RecentActivity(id = "1", title = "대학교 전공과 진로에 대해 이야기 했어요", type = ActivityType.CONVERSATION, status = "대화 완료", date = "2026.08.20", tags = listOf("학교", "진로"), summary = "서로의 전공과 관심 분야를 공유하고, 진로 고민에 대해 조언을 주고받았어요."),
        RecentActivity(id = "2", title = "여행 계획을 공유하며 추천 장소를 주고 받았어요", type = ActivityType.CONVERSATION, status = "대화 완료", date = "2026.08.20", tags = listOf("여행", "취미"), summary = "이번 주말 여행 계획을 이야기하고, 가고 싶은 장소를 추천했어요."),
        RecentActivity(id = "3", title = "처음 보는 사람에게 짧게 인사하기", type = ActivityType.CONVERSATION, status = "대화 완료", date = "2026.08.19", tags = emptyList(), summary = null) // 요약이 아직 생성 안된 케이스
    )

    private val stubSentences = mutableListOf(
        BookmarkArchiveItem(id = "1", title = "네, 안녕하세요. 항상 아메리카노만 마셨는데, 오늘은 좀 달달한 걸 먹고 싶어요.", status = "문장 저장", date = "2026.08.20", isSaved = true, memoKeywords = listOf("상황극", "요청하기", "정중함"), memoText = "자신의 평소 취향과 현재 원하는 바를 명확하고 정중하게 전달하는 표현입니다.", relatedConversationId = "1")
    )

    private val stubReports = mutableListOf(
        BookmarkArchiveItem(id = "4", title = "처음 보는 사람에게 짧게 인사하기", status = "리포트 열람", date = "2026.08.20", isSaved = true)
    )

    private val stubReportDetails = mapOf(
        "4" to Pair(
            GrowthReport(prevLevel = 1, currentLevel = 2, growthPercent = 18, weekLabels = listOf("7월 4주"), categoryRanks = listOf(CategoryRank("여행", 10)), completedMissions = 26, totalMissions = 100),
            WeeklyCompareReport(metrics = listOf(MetricChange("친절한 태도", 88, 92)), highlights = listOf(HighlightItem("전체 점수", "가 상승했어요")))
        )
    )

    private val stubConversationDetails = listOf(
        ConversationDetailMock(
            id = "1", title = "처음 보는 사람에게 짧게 인사하기", date = "2026.08.20", duration = "5분 30초", summaryKeywords = listOf("자기 성장"),
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
            return ApiResult.Success(MissionSaveResponse(missionId = id, isSaved = savedStatus, savedAt = "2026.08.20"))
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
                val mockResponse = ArchivePhraseDetailResponse(id = sentence.id, content = sentence.title, memo = sentence.memoText, missionTitle = relatedConversation?.title, conversationId = sentence.relatedConversationId, folderId = null, summaryChips = sentence.memoKeywords, createdAt = sentence.date)
                return ApiResult.Success(mockResponse)
            } else return ApiResult.Error(null, "문장을 찾을 수 없습니다.")
        } else {
            return try {
                val response = archiveApi.getPhraseDetail(id)
                if (response.data != null) ApiResult.Success(response.data) else ApiResult.Error(null, response.message ?: "오류 발생")
            } catch (e: Exception) { ApiResult.Exception(e) }
        }
    }

    suspend fun getArchiveReportDetail(id: String): ApiResult<Triple<String, GrowthReport, WeeklyCompareReport>> {
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
                        GrowthReport(
                            prevLevel = g.levelBefore,
                            currentLevel = g.levelAfter,
                            growthPercent = g.trendChangeRate.roundToInt(),
                            weekLabels = g.weeklyTrend.map { it.week },
                            categoryRanks = g.topCategories.map { CategoryRank(name = it.category, count = it.count) },
                            completedMissions = g.missionProgress.completed,
                            totalMissions = g.missionProgress.total
                        )
                    } ?: GrowthReport(
                        prevLevel = 0, currentLevel = 0, growthPercent = 0,
                        weekLabels = emptyList(), categoryRanks = emptyList(),
                        completedMissions = 0, totalMissions = 0
                    )

                    val weekly = data.weeklyCompare?.let { wc ->
                        WeeklyCompareReport(
                            metrics = wc.metricChanges.map {
                                MetricChange(name = it.label.ifBlank { it.key }, lastWeek = it.from, thisWeek = it.to)
                            },
                            highlights = wc.highlights.map {
                                HighlightItem(emphasis = "", rest = it)
                            }
                        )
                    } ?: WeeklyCompareReport(
                        metrics = emptyList(),
                        highlights = emptyList()
                    )

                    val displayTitle = data.title ?: data.period ?: data.weeklyComparePeriod ?: "톡깨 리포트"

                    ApiResult.Success(Triple(displayTitle, growth, weekly))
                } else {
                    ApiResult.Error(null, response.message ?: "오류가 발생했습니다.")
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
                    durationMinutes = 5,
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

            // 💡 API 명세 변경: referenceId 매핑
            stubMissions.filter { it.isCompleted && it.isSaved }.forEach { allMockActivities.add(ArchiveRecentActivity(id = it.id, referenceId = it.id, type = "mission", title = it.title, isBookmarked = it.isSaved, missionStatus = "completed", category = it.category, difficulty = it.difficulty, estimatedMinutes = it.duration, rewardXp = it.xp, createdAt = it.completedDate)) }

            // 💡 Mock 응답에 tags, description, referenceId 포함
            stubConversations.forEach { allMockActivities.add(ArchiveRecentActivity(id = it.id, referenceId = it.id, type = "conversation", title = it.title, isBookmarked = false, createdAt = it.date, tags = it.tags, description = it.summary)) }

            stubSentences.filter { it.isSaved }.forEach { allMockActivities.add(ArchiveRecentActivity(id = it.id, referenceId = it.id, type = "phrase", title = it.title, isBookmarked = true, createdAt = it.date)) }

            // 💡 API 명세 변경: 리포트 타입에 reportType="growth" 추가 매핑
            stubReports.filter { it.isSaved }.forEach { allMockActivities.add(ArchiveRecentActivity(id = it.id, referenceId = it.id, type = "report", reportType = "growth", title = it.title, isBookmarked = true, createdAt = it.date)) }

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
        endDate: String? = null, sort: String? = null, missionFilter: String? = null, folderId: String? = null, // 💡 API 명세 변경: missionFilter 추가
        tag: String? = null, page: Int? = null, size: Int? = null
    ): ApiResult<ArchiveSearchResponse> {
        if (isMockMode) {
            val allMockItems = mutableListOf<ArchiveSearchItem>()

            // 💡 Mock 데이터 missionFilter 필터링 반영 (all, completed, incomplete)
            val filteredMissions = when (missionFilter) {
                "completed" -> stubMissions.filter { it.isCompleted }
                "incomplete" -> stubMissions.filter { !it.isCompleted }
                else -> stubMissions
            }

            // 💡 API 명세 변경: referenceId 매핑
            filteredMissions.forEach { allMockItems.add(ArchiveSearchItem(id = it.id, referenceId = it.id, type = "mission", title = it.title, isBookmarked = it.isSaved, missionStatus = if (it.isCompleted) "completed" else "in_progress", category = it.category, difficulty = it.difficulty, estimatedMinutes = it.duration, rewardXp = it.xp, missionId = it.id, createdAt = it.completedDate)) }

            // 💡 Mock 응답에 tags, description, referenceId 포함
            stubConversations.forEach { allMockItems.add(ArchiveSearchItem(id = it.id, referenceId = it.id, type = "conversation", title = it.title, isBookmarked = false, tags = it.tags, description = it.summary, createdAt = it.date)) }

            stubSentences.forEach { allMockItems.add(ArchiveSearchItem(id = it.id, referenceId = it.id, type = "phrase", title = it.title, isBookmarked = it.isSaved, createdAt = it.date)) }

            // 💡 API 명세 변경: 리포트 타입에 reportType="growth" 추가 매핑
            stubReports.forEach { allMockItems.add(ArchiveSearchItem(id = it.id, referenceId = it.id, type = "report", reportType = "growth", title = it.title, isBookmarked = it.isSaved, createdAt = it.date)) }

            return ApiResult.Success(ArchiveSearchResponse(totalCount = allMockItems.size, items = allMockItems, pageInfo = PageInfo(allMockItems.size, 1, 0)))
        } else {
            return try {
                val response = archiveApi.searchArchives(keyword, type, startDate, endDate, sort, missionFilter, folderId, tag, page, size)
                if (response.data != null) ApiResult.Success(response.data) else ApiResult.Error(null, response.message ?: "오류가 발생했습니다.")
            } catch (e: Exception) { ApiResult.Exception(e) }
        }
    }
}