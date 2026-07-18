package com.talkqquest.app.feature.archive.data

import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.core.network.safeApiCall
import com.talkqquest.app.feature.archive.data.model.ArchiveRecentActivity
import com.talkqquest.app.feature.archive.data.model.ArchiveSummary
import com.talkqquest.app.feature.archive.ui.ArchiveMissionItem
import com.talkqquest.app.feature.archive.ui.BookmarkArchiveItem
import com.talkqquest.app.feature.archive.viewmodel.ActivityType
import com.talkqquest.app.feature.archive.viewmodel.RecentActivity
import javax.inject.Inject

// 아카이브 Repository. ViewModel과 API 사이를 잇는 계층.
class ArchiveRepository @Inject constructor(
    // private val archiveApi: ArchiveApi
) {
    // =========================================================================
    // 💡 [단일 진실 공급원] 모든 화면이 공유하는 원본 더미 데이터
    // =========================================================================

    // 1. 미션 (3개) - 현재 날짜(07.18) 이전으로 수정
    private val stubMissions = listOf(
        ArchiveMissionItem(1L, "처음 보는 사람에게 짧게 인사하기", "짧은 대화", "쉬움", 2, 20, isCompleted = true, isSaved = true, completedDate = "2026.07.16"),
        ArchiveMissionItem(2L, "최근 본 영화 이야기하기", "짧은 대화", "쉬움", 5, 20, isCompleted = false, isSaved = true, completedDate = "2026.07.15"),
        ArchiveMissionItem(3L, "학교 생활 꿀팁 나누기", "일상 대화", "보통", 8, 30, isCompleted = true, isSaved = true, completedDate = "2026.07.14")
    )

    // 2. 대화 (3개) - 현재 날짜 이전으로 수정
    private val stubConversations = listOf(
        RecentActivity(id = "1", title = "처음 보는 사람에게 짧게 인사하기", type = ActivityType.CONVERSATION, status = "대화 완료", date = "2026.07.16"),
        RecentActivity(id = "2", title = "주말에 다녀온 맛집 후기 공유하기", type = ActivityType.CONVERSATION, status = "대화 완료", date = "2026.07.15"),
        RecentActivity(id = "3", title = "단골 카페에서 메뉴 추천받기", type = ActivityType.CONVERSATION, status = "대화 완료", date = "2026.07.14")
    )

    // 3. 문장 (3개) - 현재 날짜 이전으로 수정
    private val stubSentences = listOf(
        BookmarkArchiveItem(id = "1", title = "\"그렇군요! 저도 편해서 놀랐습니다. 혹시라도 불편하신 점 있다면 알려주세요!!\"", status = "문장 저장", date = "2026.07.16", isSaved = true),
        BookmarkArchiveItem(id = "2", title = "\"아, 그 영화 저도 봤어요! 특히 마지막 액션 장면이 정말 인상 깊더라고요.\"", status = "문장 저장", date = "2026.07.15", isSaved = true),
        BookmarkArchiveItem(id = "3", title = "\"오늘은 날씨가 꽤 선선하네요. 이런 날에는 산책하기 딱 좋은 것 같아요.\"", status = "문장 저장", date = "2026.07.14", isSaved = true)
    )

    // 4. 리포트 (4개) - 현재 날짜 이전으로 수정
    private val stubReports = listOf(
        BookmarkArchiveItem(id = "4", title = "처음 보는 사람에게 짧게 인사하기", status = "리포트 열람", date = "2026.07.16", isSaved = true),
        BookmarkArchiveItem(id = "5", title = "동아리 첫 모임에서 자기소개하기", status = "리포트 열람", date = "2026.07.15", isSaved = true),
        BookmarkArchiveItem(id = "6", title = "팀 프로젝트 역할 분담 회의하기", status = "리포트 열람", date = "2026.07.14", isSaved = true),
        BookmarkArchiveItem(id = "7", title = "엘리베이터에서 이웃과 스몰토크", status = "리포트 열람", date = "2026.07.13", isSaved = true)
    )

    // =========================================================================
    // 💡 ViewModel에서 데이터를 꺼내갈 수 있도록 열어둔 함수들
    // =========================================================================

    fun getArchiveMissions(): List<ArchiveMissionItem> = stubMissions
    fun getArchiveConversations(): List<RecentActivity> = stubConversations
    fun getArchiveSentences(): List<BookmarkArchiveItem> = stubSentences
    fun getArchiveReports(): List<BookmarkArchiveItem> = stubReports

    // TODO(서버 연동 전 임시): 백엔드 아카이브 API가 붙으면 아래 stub 리턴을 지우고 주석 처리된 리턴으로 복구.
    // suspend fun getArchiveSummary(): ApiResult<ArchiveSummary> = safeApiCall { archiveApi.getArchiveSummary() }

    suspend fun getArchiveSummary(): ApiResult<ArchiveSummary> {
        val summary = ArchiveSummary(
            completedMissionCount = stubMissions.count { it.isCompleted },
            conversationCount = stubConversations.size,
            savedSentenceCount = stubSentences.count { it.isSaved },
            reportCount = stubReports.size,

            recentActivities = listOf(
                ArchiveRecentActivity("1", "MISSION", stubMissions[0].title, "미션 완료", stubMissions[0].completedDate),
                ArchiveRecentActivity("2", "CONVERSATION", stubConversations[0].title, stubConversations[0].status, stubConversations[0].date),
                ArchiveRecentActivity("3", "SENTENCE", stubSentences[0].title, stubSentences[0].status, stubSentences[0].date),
                ArchiveRecentActivity("4", "REPORT", stubReports[0].title, stubReports[0].status, stubReports[0].date)
            )
        )
        return ApiResult.Success(summary)
    }
}