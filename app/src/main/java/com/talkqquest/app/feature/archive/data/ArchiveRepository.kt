package com.talkqquest.app.feature.archive.data

import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.archive.data.model.ArchiveRecentActivity
import com.talkqquest.app.feature.archive.data.model.ArchiveSummary
import com.talkqquest.app.feature.archive.data.model.ConversationDetailMock
import com.talkqquest.app.feature.archive.data.model.ReviewChatMessage
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

@Singleton
class ArchiveRepository @Inject constructor() {

    private val stubMissions = mutableListOf(
        ArchiveMissionItem(1L, "처음 보는 사람에게 짧게 인사하기", "짧은 대화", "쉬움", 2, 20, isCompleted = true, isSaved = true, completedDate = "2026.07.16"),
        ArchiveMissionItem(2L, "최근 본 영화 이야기하기", "짧은 대화", "쉬움", 5, 20, isCompleted = false, isSaved = true, completedDate = "2026.07.15"),
        ArchiveMissionItem(3L, "학교 생활 꿀팁 나누기", "일상 대화", "보통", 8, 30, isCompleted = true, isSaved = true, completedDate = "2026.07.14")
    )

    private val stubConversations = mutableListOf(
        RecentActivity(id = "1", title = "처음 보는 사람에게 짧게 인사하기", type = ActivityType.CONVERSATION, status = "대화 완료", date = "2026.07.16"),
        RecentActivity(id = "2", title = "주말에 다녀온 맛집 후기 공유하기", type = ActivityType.CONVERSATION, status = "대화 완료", date = "2026.07.15"),
        RecentActivity(id = "3", title = "단골 카페에서 메뉴 추천받기", type = ActivityType.CONVERSATION, status = "대화 완료", date = "2026.07.14")
    )

    private val stubSentences = mutableListOf(
        BookmarkArchiveItem(
            id = "1",
            title = "\"그렇군요! 저도 편해서 놀랐어요\"",
            status = "문장 저장",
            date = "2026.07.16",
            isSaved = true,
            memoKeywords = listOf("자기 성장", "첫 만남", "스몰 토크"),
            memoText = "상대방의 감정을 자연스럽게 열어줄 수 있는 좋은 문장이에요.",
            relatedConversationId = "1"
        ),
        BookmarkArchiveItem(
            id = "2",
            title = "\"완전 좋아하지! 다음에 나도 한번 가봐야겠다. 추천 고마워!\"",
            status = "문장 저장",
            date = "2026.07.15",
            isSaved = true,
            memoKeywords = listOf("일상 대화", "취향 공유", "리액션"),
            memoText = "상대방의 추천에 긍정적으로 호응하며 기분을 좋게 만드는 완벽한 리액션입니다.",
            relatedConversationId = "2"
        ),
        BookmarkArchiveItem(
            id = "3",
            title = "\"네, 안녕하세요. 항상 아메리카노만 마셨는데, 오늘은 좀 달달한 걸 먹고 싶어요.\"",
            status = "문장 저장",
            date = "2026.07.14",
            isSaved = true,
            memoKeywords = listOf("상황극", "요청하기", "정중함"),
            memoText = "자신의 평소 취향과 현재 원하는 바를 명확하고 정중하게 전달하는 표현입니다.",
            relatedConversationId = "3"
        )
    )

    private val stubReports = mutableListOf(
        BookmarkArchiveItem(id = "4", title = "처음 보는 사람에게 짧게 인사하기", status = "리포트 열람", date = "2026.07.16", isSaved = true),
        BookmarkArchiveItem(id = "5", title = "동아리 첫 모임에서 자기소개하기", status = "리포트 열람", date = "2026.07.15", isSaved = true),
        BookmarkArchiveItem(id = "6", title = "팀 프로젝트 역할 분담 회의하기", status = "리포트 열람", date = "2026.07.14", isSaved = true),
        BookmarkArchiveItem(id = "7", title = "엘리베이터에서 이웃과 스몰토크", status = "리포트 열람", date = "2026.07.13", isSaved = true)
    )

    private val stubReportDetails = mapOf(
        "4" to Pair(
            GrowthReport(
                prevLevel = 1, currentLevel = 2, growthPercent = 18,
                weekLabels = listOf("7월 4주", "8월 1주", "8월 2주", "8월 3주"),
                categoryRanks = listOf(
                    CategoryRank("여행", 10), CategoryRank("음식", 9),
                    CategoryRank("일상", 7), CategoryRank("인사", 4)
                ),
                completedMissions = 26, totalMissions = 100
            ),
            WeeklyCompareReport(
                metrics = listOf(
                    MetricChange("친절한 태도", 88, 92), MetricChange("대화 주도", 86, 88),
                    MetricChange("공감 표현", 82, 85), MetricChange("질문 연결성", 74, 78)
                ),
                highlights = listOf(
                    HighlightItem("전체 점수", "가 78점에서 86점으로 상승했어요"),
                    HighlightItem("친절한 태도", "가 가장 많이 상승되었어요"),
                    HighlightItem("질문 연결성", "을 꾸준히 개선하고 있어요")
                )
            )
        ),
        "5" to Pair(
            GrowthReport(
                prevLevel = 2, currentLevel = 3, growthPercent = 24,
                weekLabels = listOf("7월 3주", "7월 4주", "8월 1주", "8월 2주"),
                categoryRanks = listOf(
                    CategoryRank("학교", 12), CategoryRank("일상", 8),
                    CategoryRank("모임", 6), CategoryRank("음식", 3)
                ),
                completedMissions = 32, totalMissions = 100
            ),
            WeeklyCompareReport(
                metrics = listOf(
                    MetricChange("친절한 태도", 90, 94), MetricChange("대화 주도", 78, 84),
                    MetricChange("공감 표현", 80, 86), MetricChange("질문 연결성", 82, 88)
                ),
                highlights = listOf(
                    HighlightItem("대화 주도", "가 눈에 띄게 좋아졌어요"),
                    HighlightItem("질문 연결성", "이 크게 향상되었어요"),
                    HighlightItem("공감 표현", "도 자연스럽게 늘고 있어요")
                )
            )
        ),
        "6" to Pair(
            GrowthReport(
                prevLevel = 3, currentLevel = 3, growthPercent = 5,
                weekLabels = listOf("7월 2주", "7월 3주", "7월 4주", "8월 1주"),
                categoryRanks = listOf(
                    CategoryRank("업무", 15), CategoryRank("학교", 10),
                    CategoryRank("설득", 8), CategoryRank("일상", 5)
                ),
                completedMissions = 40, totalMissions = 100
            ),
            WeeklyCompareReport(
                metrics = listOf(
                    MetricChange("친절한 태도", 85, 85), MetricChange("대화 주도", 92, 95),
                    MetricChange("공감 표현", 70, 75), MetricChange("질문 연결성", 88, 90)
                ),
                highlights = listOf(
                    HighlightItem("공감 표현", "이 이전보다 훨씬 부드러워졌어요"),
                    HighlightItem("대화 주도", "능력이 팀 회의에서 돋보여요"),
                    HighlightItem("전체 점수", "가 안정적으로 유지되고 있어요")
                )
            )
        ),
        "7" to Pair(
            GrowthReport(
                prevLevel = 1, currentLevel = 1, growthPercent = 12,
                weekLabels = listOf("7월 1주", "7월 2주", "7월 3주", "7월 4주"),
                categoryRanks = listOf(
                    CategoryRank("인사", 14), CategoryRank("이웃", 9),
                    CategoryRank("일상", 6), CategoryRank("날씨", 4)
                ),
                completedMissions = 15, totalMissions = 100
            ),
            WeeklyCompareReport(
                metrics = listOf(
                    MetricChange("친절한 태도", 95, 98), MetricChange("대화 주도", 60, 68),
                    MetricChange("공감 표현", 85, 88), MetricChange("질문 연결성", 65, 72)
                ),
                highlights = listOf(
                    HighlightItem("질문 연결성", "이 눈에 띄게 좋아졌어요"),
                    HighlightItem("대화 주도", "점수가 꾸준히 오르고 있어요"),
                    HighlightItem("친절한 태도", "는 항상 훌륭해요")
                )
            )
        )
    )

    private val stubConversationDetails = listOf(
        ConversationDetailMock(
            id = "1",
            title = "처음 보는 사람에게 짧게 인사하기",
            date = "2026.07.16",
            duration = "5분 30초",
            summaryKeywords = listOf("자기 성장", "첫 만남", "스몰 토크"),
            summaryText = "카페에서 처음 만난 사람에게 자연스럽게 인사를 건네고, 간단한 질문을 이어가며 어색하지 않게 대화를 시작하는 연습을 진행했습니다.",
            mainContentText = "먼저 인사를 건네며 대화를 시작했어요. \"자주는 오시나요?\"와 같은 질문으로 대화를 이어갔어요. 상대의 답변에 반응하고 공감하며 대화를 마무리했어요.",
            feedbacks = listOf("친절한 태도" to 92, "대화 주도" to 88, "공감 능력" to 85, "질문 연결성" to 78),
            messages = listOf(
                ReviewChatMessage("1", "안녕하세요! 처음 뵙네요 \uD83D\uDE42", false, "9:20"),
                ReviewChatMessage("2", "오늘 여기 처음 오셨어요?", false, "9:20"),
                ReviewChatMessage("3", "분위기가 좋아보여서요!", true, "9:21"),
                ReviewChatMessage("4", "오, 그러셨구나. 저는 여기 몇 번 와봤는데 생각보다 괜찮더라고요", false, "9:21"),
                ReviewChatMessage("5", "오 그렇군요!", true, "9:21"),
                ReviewChatMessage("6", "혹시 이런 곳 자주 다니세요?", true, "9:21"),
                ReviewChatMessage("7", "자주는 아니고 가끔 생각날 때 오는 편이에요. 분위기가 편해서 좋더라고요", false, "9:21"),
                ReviewChatMessage("8", "그렇군요! 저도 편해서 놀랐어요", true, "9:21"),
                ReviewChatMessage("9", "맞아요ㅎㅎ 처음 와도 부담이 없어서 좋은 것 같아요.", false, "9:21")
            )
        ),
        ConversationDetailMock(
            id = "2",
            title = "주말에 다녀온 맛집 후기 공유하기",
            date = "2026.07.15",
            duration = "7분 15초",
            summaryKeywords = listOf("일상 대화", "취향 공유", "리액션"),
            summaryText = "친구에게 주말에 다녀온 맛집의 분위기와 추천 메뉴를 생생하게 설명하고, 상대방의 반응을 이끌어내는 대화를 나누었습니다.",
            mainContentText = "\"주말에 새로 생긴 파스타 집에 갔는데 진짜 맛있더라!\"라며 대화를 열었어요. 음식 맛뿐만 아니라 인테리어에 대한 느낌도 구체적으로 묘사했습니다. 상대방이 좋아하는 음식 취향도 자연스럽게 물어봤어요.",
            feedbacks = listOf("친절한 태도" to 95, "대화 주도" to 82, "공감 능력" to 88, "질문 연결성" to 85),
            messages = listOf(
                ReviewChatMessage("1", "주말에 새로 생긴 파스타 집에 갔는데 진짜 맛있더라!", true, "12:30"),
                ReviewChatMessage("2", "오 진짜? 어디 있는 곳이야? 나도 파스타 좋아하는데!", false, "12:30"),
                ReviewChatMessage("3", "연남동 쪽에 있는 곳인데, 인테리어도 엄청 예뻤어.", true, "12:31"),
                ReviewChatMessage("4", "분위기 좋은 맛집인가 보네. 제일 맛있었던 메뉴는 뭐야?", false, "12:31"),
                ReviewChatMessage("5", "트러플 크림 파스타! 너도 크림 파스타 좋아해?", true, "12:32"),
                ReviewChatMessage("6", "완전 좋아하지! 다음에 나도 한번 가봐야겠다. 추천 고마워!", false, "12:32")
            )
        ),
        ConversationDetailMock(
            id = "3",
            title = "단골 카페에서 메뉴 추천받기",
            date = "2026.07.14",
            duration = "8분 40초",
            summaryKeywords = listOf("상황극", "요청하기", "정중함"),
            summaryText = "카페 직원에게 평소 마시던 메뉴 대신 새로운 음료를 추천해 달라고 정중하게 요청하고, 취향을 설명하는 연습을 했습니다.",
            mainContentText = "\"항상 아메리카노만 마셨는데, 오늘은 좀 달달한 걸 먹고 싶어요\"라고 취향을 명확히 전달했어요. 직원이 추천해준 메뉴에 대해 어떤 맛인지 추가로 질문하며 대화를 이어갔습니다. 마지막엔 감사 인사도 잊지 않았어요.",
            feedbacks = listOf("친절한 태도" to 90, "대화 주도" to 75, "공감 능력" to 80, "질문 연결성" to 92),
            messages = listOf(
                ReviewChatMessage("1", "안녕하세요! 주문 도와드릴까요?", false, "15:00"),
                ReviewChatMessage("2", "네, 안녕하세요. 항상 아메리카노만 마셨는데, 오늘은 좀 달달한 걸 먹고 싶어요.", true, "15:00"),
                ReviewChatMessage("3", "아, 달콤한 음료 찾으시는구나! 혹시 커피 들어간 거 괜찮으세요?", false, "15:00"),
                ReviewChatMessage("4", "네, 커피 들어간 걸로 추천해 주시겠어요?", true, "15:01"),
                ReviewChatMessage("5", "그럼 저희 시그니처인 바닐라 크림 콜드브루는 어떠세요? 많이 달지 않고 부드러워요.", false, "15:01"),
                ReviewChatMessage("6", "오, 그거 좋네요. 어떤 맛인지 궁금해요!", true, "15:02"),
                ReviewChatMessage("7", "은은한 바닐라 향이랑 깔끔한 콜드브루가 잘 어울려서 인기 메뉴예요. 시럽은 원하시면 조절 가능합니다.", false, "15:02"),
                ReviewChatMessage("8", "그럼 시럽은 기본으로 해서 그걸로 한 잔 주세요.", true, "15:03"),
                ReviewChatMessage("9", "네, 알겠습니다. 사이즈는 어떻게 준비해 드릴까요? 레귤러와 라지 사이즈가 있습니다.", false, "15:03"),
                ReviewChatMessage("10", "라지 사이즈로 할게요. 아, 혹시 디카페인으로도 변경 가능한가요?", true, "15:04"),
                ReviewChatMessage("11", "네, 디카페인 원두로 변경 가능합니다. 500원 추가되는데 괜찮으신가요?", false, "15:04"),
                ReviewChatMessage("12", "네 괜찮아요. 그리고 테이크아웃 할게요.", true, "15:04"),
                ReviewChatMessage("13", "알겠습니다. 바닐라 크림 콜드브루 라지 사이즈, 디카페인 변경해서 테이크아웃으로 준비해 드릴게요. 결제 도와드리겠습니다.", false, "15:05"),
                ReviewChatMessage("14", "여기 카드요. 아, 그리고 영수증은 버려주세요.", true, "15:05"),
                ReviewChatMessage("15", "네, 결제 완료되었습니다. 주문하신 음료는 저쪽 픽업대에서 금방 준비해 드릴게요.", false, "15:06"),
                ReviewChatMessage("16", "네, 감사합니다. 수고하세요!", true, "15:06"),
                ReviewChatMessage("17", "감사합니다! 좋은 하루 보내세요~", false, "15:06")
            )
        )
    )

    fun toggleMissionBookmark(id: Long) {
        val index = stubMissions.indexOfFirst { it.id == id }
        if (index != -1) {
            stubMissions[index] = stubMissions[index].copy(isSaved = !stubMissions[index].isSaved)
        }
    }

    fun toggleSentenceBookmark(id: String) {
        val index = stubSentences.indexOfFirst { it.id == id }
        if (index != -1) {
            stubSentences[index] = stubSentences[index].copy(isSaved = !stubSentences[index].isSaved)
        }
    }

    fun toggleReportBookmark(id: String) {
        val index = stubReports.indexOfFirst { it.id == id }
        if (index != -1) {
            stubReports[index] = stubReports[index].copy(isSaved = !stubReports[index].isSaved)
        }
    }

    fun getArchiveMissions(): List<ArchiveMissionItem> = stubMissions.toList()
    fun getArchiveConversations(): List<RecentActivity> = stubConversations.toList()
    fun getArchiveSentences(): List<BookmarkArchiveItem> = stubSentences.toList()
    fun getArchiveReports(): List<BookmarkArchiveItem> = stubReports.toList()

    fun getConversationDetail(id: String): ConversationDetailMock? {
        return stubConversationDetails.find { it.id == id }
    }

    fun getSavedSentenceDetail(id: String): Pair<BookmarkArchiveItem, RecentActivity?>? {
        val sentence = stubSentences.find { it.id == id } ?: return null
        val relatedConversation = stubConversations.find { it.id == sentence.relatedConversationId }
        return Pair(sentence, relatedConversation)
    }

    fun getArchiveReportDetail(id: String): Triple<String, GrowthReport, WeeklyCompareReport>? {
        val title = stubReports.find { it.id == id }?.title ?: "성장 리포트"
        val reports = stubReportDetails[id] ?: return null
        return Triple(title, reports.first, reports.second)
    }

    suspend fun getArchiveSummary(): ApiResult<ArchiveSummary> {
        val allMockActivities = mutableListOf<ArchiveRecentActivity>()

        // 💡 최근 활동 리스트에도 저장(isSaved)된 항목만 노출하도록 변경
        stubMissions.filter { it.isCompleted && it.isSaved }.forEach {
            allMockActivities.add(ArchiveRecentActivity(it.id.toString(), "MISSION", it.title, "미션 완료", it.completedDate))
        }
        stubConversations.forEach {
            allMockActivities.add(ArchiveRecentActivity(it.id, "CONVERSATION", it.title, it.status, it.date))
        }
        stubSentences.filter { it.isSaved }.forEach {
            allMockActivities.add(ArchiveRecentActivity(it.id, "SENTENCE", it.title, it.status, it.date))
        }
        stubReports.filter { it.isSaved }.forEach {
            allMockActivities.add(ArchiveRecentActivity(it.id, "REPORT", it.title, it.status, it.date))
        }

        val top4RecentActivities = allMockActivities
            .sortedByDescending { it.date }
            .take(4)

        // 💡 모든 카테고리 숫자가 isSaved(저장 여부)를 기준으로 실시간 카운트되도록 수정
        val summary = ArchiveSummary(
            completedMissionCount = stubMissions.count { it.isSaved },
            conversationCount = stubConversations.size,
            savedSentenceCount = stubSentences.count { it.isSaved },
            reportCount = stubReports.count { it.isSaved },
            recentActivities = top4RecentActivities
        )
        return ApiResult.Success(summary)
    }
}