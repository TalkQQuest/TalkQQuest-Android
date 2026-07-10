package com.talkqquest.app.feature.mission.data

import com.talkqquest.app.core.datastore.UserXpStore
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.core.network.safeApiCall
import com.talkqquest.app.feature.mission.data.model.ConversationPrep
import com.talkqquest.app.feature.mission.data.model.FeedbackResult
import com.talkqquest.app.feature.mission.data.model.MissionCompleteResult
import com.talkqquest.app.feature.mission.data.model.MissionDetail
import com.talkqquest.app.feature.mission.data.model.MissionListItem
import javax.inject.Inject
import javax.inject.Singleton

// 미션 Repository. ViewModel과 API 사이 계층 (홈 패턴과 동일).
// @Singleton: 앱에 1개만 만들어 모든 화면이 같은 인스턴스를 씀 — 북마크 상태 공유의 전제.
@Singleton
class MissionRepository @Inject constructor(
    private val missionApi: MissionApi,
    private val userXpStore: UserXpStore, // 서버 전 임시: 완료 XP를 홈과 공유
) {
    // ── 서버 연동 전 임시: 북마크 상태 공유 ──
    // 화면(ViewModel)마다 stub을 복사해 들면 토글이 서로 안 보임(목록에서 저장해도 저장 목록에 안 뜸)
    // → 토글 결과를 여기 한 곳에 모아 모든 화면이 같은 상태를 봄.
    // TODO(서버 연동): 이 map 지우고 toggleSave를 POST/DELETE /api/v1/missions/{id}/save 호출로 교체.
    private val savedOverrides = mutableMapOf<Long, Boolean>()

    // 완료 처리 공유 (북마크와 같은 이유) — 완료한 미션이 목록/저장 목록의 상태 필터에도 반영되게.
    // TODO(서버 연동): 완료 API가 상태를 저장하면 이 map 삭제.
    private val statusOverrides = mutableMapOf<Long, String>()

    fun toggleSave(missionId: Long): Boolean {
        val base = stubMissions.firstOrNull { it.id == missionId }?.isSaved ?: false
        val now = !(savedOverrides[missionId] ?: base)
        savedOverrides[missionId] = now
        return now
    }

    // stub 원본 위에 토글된 북마크·완료 상태를 덮어씀
    private fun MissionListItem.applySaved(): MissionListItem {
        var item = savedOverrides[id]?.let { copy(isSaved = it) } ?: this
        statusOverrides[id]?.let { item = item.copy(status = it) }
        return item
    }

    // TODO(서버 연동 전 임시): 백엔드 미션 API 붙으면 아래 stub 리턴 지우고 return 한 줄로 복구.
    //     suspend fun getMissions() = safeApiCall { missionApi.getMissions() }
    suspend fun getMissions(): ApiResult<List<MissionListItem>> =
        ApiResult.Success(stubMissions.map { it.applySaved() })

    // TODO(서버 연동 전 임시): 붙으면 return safeApiCall { missionApi.getMissionDetail(missionId) } 로 복구.
    suspend fun getMissionDetail(missionId: Long): ApiResult<MissionDetail> {
        val item = stubMissions.firstOrNull { it.id == missionId }
            ?: return ApiResult.Error(code = 404, message = "미션을 찾을 수 없어요.")
        return ApiResult.Success(
            MissionDetail(
                id = item.id,
                title = item.title,
                category = item.category,
                difficulty = item.difficulty,
                estimatedMinutes = item.estimatedMinutes,
                rewardXp = item.rewardXp,
                isSaved = item.applySaved().isSaved,
                benefits = stubBenefits[item.id] ?: defaultBenefits,
            ),
        )
    }

    // TODO(서버 연동 전 임시): 붙으면 return safeApiCall { missionApi.getConversationPrep(missionId) } 로 복구.
    // 새로고침(refresh=true) 시 서버는 새 문장을 주지만, stub은 문장 묶음을 번갈아 돌려 "바뀌는" 느낌만 재현.
    suspend fun getConversationPrep(missionId: Long, refreshIndex: Int = 0): ApiResult<ConversationPrep> {
        val opener = stubOpenerSets[refreshIndex % stubOpenerSets.size]
        return ApiResult.Success(ConversationPrep(topics = stubTopics, openers = opener))
    }

    // ── 대화 진행 stub (목업 대사 그대로 시나리오) ──
    // TODO(서버 연동): AI 대화 API로 교체 — 시작/응답/추천 답변 각각 서버가 줌.
    suspend fun getConversationIntro(missionId: Long): ApiResult<List<String>> =
        ApiResult.Success(listOf("안녕하세요! 처음 뵙네요 🙂", "오늘 여기 처음 오셨어요?"))

    suspend fun getAiReply(turnIndex: Int): ApiResult<String> =
        ApiResult.Success(stubAiReplies[turnIndex % stubAiReplies.size])

    suspend fun getRecommendedReplies(turnIndex: Int): ApiResult<List<String>> =
        ApiResult.Success(stubRecommendationSets[turnIndex % stubRecommendationSets.size])

    // 미션 완료 처리 + 결과(체크리스트·XP·레벨) 조회.
    // TODO(서버 연동 전 임시): 붙으면 return safeApiCall { missionApi.completeMission(missionId) } 로 교체
    //     — 체크리스트 문구·개수, XP·레벨 계산 전부 서버 몫.
    suspend fun completeMission(missionId: Long): ApiResult<MissionCompleteResult> {
        val gained = stubMissions.firstOrNull { it.id == missionId }?.rewardXp ?: 20
        statusOverrides[missionId] = "완료" // 목록·저장 목록의 상태 필터에 바로 반영
        val beforeXp = userXpStore.currentXp
        val beforeLevel = userXpStore.level
        // 재완료도 매번 지급 — 기능명세서 기준(대화 종료 = MissionRecord+XpHistory+XP 증가 트랜잭션,
        // 재완료 제한 규칙 없음). 제한할지는 기획 확인거리.
        userXpStore.addXp(gained)
        return ApiResult.Success(
            MissionCompleteResult(
                checklist = stubCompleteChecklist,
                gainedXp = gained,
                levelBefore = beforeLevel,
                levelAfter = userXpStore.level,
                xpBefore = beforeXp,
                xpAfter = userXpStore.currentXp,
                nextLevelXp = userXpStore.nextLevelXp,
            ),
        )
    }

    // AI 피드백 조회 (E101). 실제 응답은 피드백당 1세트(항목별 아님)지만, stub은 상세 화면의
    // 문구 길이 대응을 확인할 수 있게 itemIndex(요약에서 탭한 행)별로 짧음/중간/김/아주 김을 돌려줌.
    // itemIndex 생략(요약 화면) = 목업 값 그대로.
    // TODO(서버 연동 전 임시): 붙으면 POST /api/v1/feedback 생성 → GET /api/v1/feedback/{feedbackId}
    //     조회로 교체(itemIndex 분기 삭제) — 값 계산 전부 서버 몫. stub은 missionId를 feedbackId로 받음.
    suspend fun getFeedback(feedbackId: Long, itemIndex: Int = -1): ApiResult<FeedbackResult> {
        val texts = stubFeedbackTexts.getOrElse(itemIndex) { stubFeedbackTexts[0] }
        return ApiResult.Success(
            FeedbackResult(
                nickname = "다민", // TODO(서버 연동): 유저 프로필 닉네임으로 교체
                kindnessScore = 92,
                initiativeScore = 88,
                empathyScore = 85,
                questionLinkScore = 78,
                strengths = texts.first,
                improvements = texts.second,
                savedPhrase = texts.third,
            ),
        )
    }
}

// 피드백 상세 문구 stub — 항목(행)별로 길이를 다르게 해 레이아웃 검증용(자작 예시 문구).
// [0]=목업 그대로(중간), [1]=아주 짧음, [2]=김(2줄 감김), [3]=아주 김(3줄+짧은 것 혼합).
// 서버 연동 시 통째 삭제 (실제 응답은 피드백당 1세트).
private val stubFeedbackTexts = listOf(
    Triple(
        listOf("상대를 존중하는 표현을 사용했어요", "대화를 따뜻하게 시작했어요", "긍정적인 말투를 유지했어요"),
        listOf("조금 더 구체적인 칭찬을 해보세요", "상대의 감정을 확인하는 표현을 사용해보세요"),
        "그렇군요! 저도 생각보다 편해서 놀랐어요",
    ),
    Triple(
        listOf("먼저 인사를 건넸어요"),
        listOf("답을 기다려보세요"),
        "여기 자주 오세요?",
    ),
    Triple(
        listOf(
            "상대의 말에 \"정말 그러셨겠어요\"처럼 감정을 받아주는 표현을 자주 사용했어요",
            "상대가 힘들었던 이야기를 할 때 화제를 돌리지 않고 끝까지 들어줬어요",
            "비슷한 경험을 나누면서 상대와 감정을 공유했어요",
        ),
        listOf(
            "공감한 뒤에 상대의 감정을 한 번 더 확인하는 질문을 덧붙이면 대화가 더 깊어져요",
            "리액션이 반복되지 않게 표현을 조금씩 바꿔보세요",
        ),
        "정말 고생 많으셨겠어요. 저라도 그 상황에서는 많이 힘들었을 것 같아요",
    ),
    Triple(
        listOf(
            "앞선 대화 내용을 기억했다가 \"아까 말씀하신 영화\"처럼 이전 주제를 자연스럽게 다시 끌어와서 질문을 이어갔어요. 대화가 끊기지 않고 흘러가게 만드는 아주 좋은 습관이에요",
            "상대의 대답에서 새로운 키워드를 찾아 다음 질문으로 연결했어요",
            "네",
        ),
        listOf(
            "질문이 연달아 이어지면 상대가 취조받는 느낌을 받을 수 있어요. 질문 두세 개마다 내 이야기를 한 번씩 섞어서 대화의 주고받는 리듬을 만들어보세요. 예를 들어 상대의 취미를 물어본 다음에는 내 취미도 짧게 소개하는 식이에요",
            "닫힌 질문(네/아니오)보다 열린 질문을 써보세요",
        ),
        "아까 여행 좋아하신다고 하셨잖아요, 최근에 다녀온 곳 중에 제일 기억에 남는 데는 어디였어요?",
    ),
)

// 미션 완료 체크리스트 stub — 목업 4개 그대로. 서버 연동 시 통째 삭제.
private val stubCompleteChecklist = listOf(
    "장소 경험을 공유했어요",
    "상대의 이야기에 공감했어요",
    "자연스럽게 질문을 주고받았어요",
    "긍정적인 분위기로 대화를 마무리했어요",
)

// 대화 진행 stub 대사 — 목업 대화 흐름 그대로 순서대로 응답. 서버 연동 시 통째 삭제.
private val stubAiReplies = listOf(
    "오, 그러셨구나. 저는 여기 몇 번 와봤는데 생각보다 괜찮더라고요",
    "자주는 아니고 가끔 생각날 때 오는 편이에요. 분위기가 편해서 좋더라고요",
    "맞아요ㅎㅎ 처음 와도 부담이 없어서 좋은 것 같아요.",
    "저도 그렇게 생각해요! 이야기 나눠서 즐거웠어요 🙂",
)

// 추천 답변 묶음 — 첫 벌 = 목업 그대로, 이후 벌은 흐름에 맞는 변형.
private val stubRecommendationSets = listOf(
    listOf("그렇군요! 저도 생각보다 편해서 놀랐어요.", "맞아요. 분위기가 좋네요.", "다음에도 와보고 싶어요."),
    listOf("혹시 이런 곳 자주 다니세요?", "분위기가 정말 편하네요.", "오늘 와보길 잘했어요."),
    listOf("맞아요, 저도 그렇게 느꼈어요.", "다음에 또 얘기 나눠요!", "덕분에 즐거웠어요."),
)

// 대화 준비 stub. 주제(표시용) — 디자인 목업 6개 그대로.
private val stubTopics = listOf("오늘 날씨", "주말 계획", "좋아하는 음식", "최근 본 영화", "학교 생활", "취미 활동")

// 첫 마디 묶음 여러 벌 — 새로고침 때 번갈아 나와 "새 문장" 느낌. 첫 벌 = 목업 그대로.
private val stubOpenerSets = listOf(
    listOf("안녕하세요! 처음 뵙겠습니다.", "오늘 하루 잘 보내고 계신가요?", "여기 분위기 좋네요!"),
    listOf("실례지만 잠깐 얘기 나눠도 될까요?", "날씨가 참 좋네요, 그렇죠?", "여기 자주 오시나요?"),
)

// 서버 없이 에뮬/프리뷰에서 목록 확인용 임시 데이터. 서버 연동 시 통째로 삭제.
// 1~5번 = 피그마 목업 카드 그대로. 6번 = 목업에 잘려 보이는 카드(일상 대화) 추정. 7번 = 긴 제목(2줄 확장 검증용).
private val stubMissions = listOf(
    MissionListItem(
        id = 1,
        title = "처음 보는 사람에게 짧게 인사하기",
        category = "짧은 대화",
        difficulty = "쉬움",
        estimatedMinutes = 2,
        rewardXp = 20,
    ),
    MissionListItem(
        id = 2,
        title = "최근 본 영화 이야기하기",
        category = "짧은 대화",
        difficulty = "쉬움",
        estimatedMinutes = 5,
        rewardXp = 20,
        isSaved = true,
        status = "완료",
    ),
    MissionListItem(
        id = 3,
        title = "학교 생활 꿀팁 나누기",
        category = "일상 대화",
        difficulty = "보통",
        estimatedMinutes = 8,
        rewardXp = 30,
        isSaved = true,
        status = "진행중",
    ),
    MissionListItem(
        id = 4,
        title = "나의 취미를 소개해보기",
        category = "친구 만들기",
        difficulty = "어려움",
        estimatedMinutes = 10,
        rewardXp = 40,
    ),
    MissionListItem(
        id = 5,
        title = "주말 계획 이야기하기",
        category = "짧은 대화",
        difficulty = "쉬움",
        estimatedMinutes = 5,
        rewardXp = 20,
    ),
    MissionListItem(
        id = 6,
        title = "회사 생활 이야기하기",
        category = "일상 대화",
        difficulty = "보통",
        estimatedMinutes = 8,
        rewardXp = 30,
    ),
    MissionListItem(
        id = 7,
        title = "동아리에서 관심사가 비슷한 사람에게 먼저 말 걸어보기",
        category = "친구 만들기",
        difficulty = "어려움",
        estimatedMinutes = 15,
        rewardXp = 60,
    ),
)

// 미션 상세 효과 문구 stub. 1번 = 피그마 목업 문구 그대로. 3번 = 3개(개수 가변 검증용).
private val stubBenefits = mapOf(
    1L to listOf(
        "낯선 사람과의 첫 대화에 자신감이 생겨요",
        "자연스럽게 대화를 이어갈 수 있어요",
    ),
    3L to listOf(
        "일상 속 이야깃거리를 찾는 눈이 생겨요",
        "상대방과 공감대를 만들 수 있어요",
        "대화를 오래 이어가는 힘이 생겨요",
    ),
)

private val defaultBenefits = listOf(
    "대화에 자신감이 생겨요",
    "자연스럽게 대화를 이어갈 수 있어요",
)
