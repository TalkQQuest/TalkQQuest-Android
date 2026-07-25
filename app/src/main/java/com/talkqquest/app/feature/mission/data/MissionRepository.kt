package com.talkqquest.app.feature.mission.data

import com.talkqquest.app.core.datastore.UserXpStore
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.core.network.serverCall
import com.talkqquest.app.feature.mission.data.model.ConversationCreateRequest
import com.talkqquest.app.feature.mission.data.model.ConversationMessageRequest
import com.talkqquest.app.feature.mission.data.model.ConversationPrep
import com.talkqquest.app.feature.mission.data.model.FeedbackItemText
import com.talkqquest.app.feature.mission.data.model.FeedbackResult
import com.talkqquest.app.feature.mission.data.model.MissionCompleteRequest
import com.talkqquest.app.feature.mission.data.model.MissionCompleteResult
import com.talkqquest.app.feature.mission.data.model.MissionDetail
import com.talkqquest.app.feature.mission.data.model.MissionListItem
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// 미션 Repository. ViewModel과 API 사이 계층 (홈 패턴과 동일).
// @Singleton: 앱에 1개만 만들어 모든 화면이 같은 인스턴스를 씀 — 북마크 상태 공유의 전제.
// id는 서버(UUID 문자열) 기준 String — stub도 "1".."7" 문자열 사용.
@Singleton
class MissionRepository @Inject constructor(
    private val missionApi: MissionApi,
    private val userXpStore: UserXpStore, // 서버 전 임시: 완료 XP를 홈과 공유
) {
    // 북마크 낙관 토글 공유 — 모든 화면이 같은 상태를 봄 (서버 반영 전/오프라인에도 UI 일관).
    private val savedOverrides = mutableMapOf<String, Boolean>()

    // 서버 목록에서 마지막으로 확인한 저장 상태 캐시 (토글의 현재값 판단 기준).
    private val serverSaved = mutableMapOf<String, Boolean>()

    // 완료 처리 공유 — 서버 목록 응답엔 진행 상태 필드가 없어(실측) 완료 표시는 로컬 유지.
    private val statusOverrides = mutableMapOf<String, String>()

    // 현재 진행 중인 서버 대화 세션. 화면 route는 missionId를 쓰므로 세션 id는 여기 보관.
    private var activeConversationId: String? = null

    // toggleSave가 non-suspend(클릭 즉시 UI 반영)라 서버 반영은 백그라운드로.
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // 북마크 토글: 로컬 즉시 반영(낙관) + 서버 POST/DELETE /missions/{id}/save 백그라운드 반영.
    // 실패해도 UI는 유지(다음 목록 조회 때 서버 값으로 자연 정정) — 데모가 안 죽게.
    fun toggleSave(missionId: String): Boolean {
        val current = savedOverrides[missionId]
            ?: serverSaved[missionId]
            ?: stubMissions.firstOrNull { it.id == missionId }?.isSaved
            ?: false
        val now = !current
        savedOverrides[missionId] = now
        ioScope.launch {
            val r = serverCall {
                if (now) missionApi.saveMission(missionId) else missionApi.unsaveMission(missionId)
            }
            if (r is ApiResult.Success) serverSaved[missionId] = r.data.isSaved
        }
        return now
    }

    // stub 원본 위에 토글된 북마크·완료 상태를 덮어씀
    private fun MissionListItem.applySaved(): MissionListItem {
        var item = savedOverrides[id]?.let { copy(isSaved = it) } ?: this
        statusOverrides[id]?.let { item = item.copy(status = it) }
        return item
    }

    // 미션 목록 — 실서버 GET /missions (2026-07-22 실측: data.missions[] + pageInfo).
    // 서버 실패(오프라인 등) 시 stub 폴백 — 데모가 안 죽게.
    suspend fun getMissions(): ApiResult<List<MissionListItem>> =
        when (val r = serverCall { missionApi.getMissions() }) {
            is ApiResult.Success -> {
                r.data.missions.forEach { serverSaved[it.id] = it.isSaved }
                ApiResult.Success(r.data.missions.map { it.applySaved() })
            }
            else -> ApiResult.Success(stubMissions.map { it.applySaved() })
        }

    // 미션 상세 — 실서버 GET /missions/{id} (실측: description·preparationTip·caution 포함).
    // benefits(효과 문구)는 서버 응답에 없어 기본 문구로 채움. 실패 시 stub 폴백.
    suspend fun getMissionDetail(missionId: String): ApiResult<MissionDetail> {
        when (val r = serverCall { missionApi.getMissionDetail(missionId) }) {
            is ApiResult.Success -> {
                serverSaved[r.data.id] = r.data.isSaved
                return ApiResult.Success(
                    r.data.copy(
                        isSaved = savedOverrides[r.data.id] ?: r.data.isSaved,
                        benefits = r.data.benefits.ifEmpty { defaultBenefits },
                    ),
                )
            }
            else -> {
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
        }
    }

    // 대화 준비 문장 — 실서버 GET /missions/{id}/prep.
    // ★실측(2026-07-22): 전 미션 items가 빈 배열(서버 시드 없음) → 비어 있으면 stub 폴백.
    //   서버가 문장을 채우면 자동으로 실데이터가 뜨는 구조. refreshIndex는 stub 순환 전용.
    suspend fun getConversationPrep(missionId: String, refreshIndex: Int = 0): ApiResult<ConversationPrep> {
        val r = serverCall { missionApi.getMissionPrep(missionId) }
        if (r is ApiResult.Success && r.data.items.isNotEmpty()) {
            val prep = r.data.toConversationPrep()
            if (prep.topics.isNotEmpty() || prep.openers.isNotEmpty()) return ApiResult.Success(prep)
        }
        val opener = stubOpenerSets[refreshIndex % stubOpenerSets.size]
        return ApiResult.Success(ConversationPrep(topics = stubTopics, openers = opener))
    }

    // ── 대화 진행 — 실서버 연동 (2026-07-22 실측 규격) + 오프라인 stub 폴백 ──

    // 대화 시작: 서버에 대화 세션 생성(POST /conversations, selectedTopic 옵션 — 없이 생성 확인).
    // 서버는 첫 인사말(AI 발화)을 안 주므로 인트로 문구는 로컬 유지(목업 그대로).
    // 세션 생성 실패(오프라인 등) 시에도 인트로는 그대로 진행 — 이후 응답이 stub으로 폴백됨.
    suspend fun getConversationIntro(missionId: String): ApiResult<List<String>> {
        activeConversationId = null
        val r = serverCall {
            missionApi.createConversation(ConversationCreateRequest(missionId = missionId, mode = "text"))
        }
        if (r is ApiResult.Success) activeConversationId = r.data.conversationId
        return ApiResult.Success(listOf("안녕하세요! 처음 뵙네요 🙂", "오늘 여기 처음 오셨어요?"))
    }

    // 사용자 발화 전송 → AI 응답. 서버 POST /conversations/{id}/messages
    // (응답의 guideMessage.content = AI 말풍선. 서버 role명은 "guide").
    // 세션 없음/호출 실패 시 stub 대사 순환 폴백 — 대화가 끊기지 않게.
    suspend fun sendUserMessage(text: String, turnIndex: Int): ApiResult<String> {
        val cid = activeConversationId
        if (cid != null) {
            val r = serverCall {
                missionApi.sendConversationMessage(cid, ConversationMessageRequest(role = "user", content = text))
            }
            if (r is ApiResult.Success) {
                r.data.guideMessage?.content?.takeIf { it.isNotBlank() }
                    ?.let { return ApiResult.Success(it) }
            }
        }
        return ApiResult.Success(stubAiReplies[turnIndex % stubAiReplies.size])
    }

    // 추천 답변 — 서버 GET /conversations/{id}/suggestions. 비거나 실패면 stub 묶음 순환.
    suspend fun getRecommendedReplies(turnIndex: Int): ApiResult<List<String>> {
        val cid = activeConversationId
        if (cid != null) {
            val r = serverCall { missionApi.getConversationSuggestions(cid) }
            if (r is ApiResult.Success && r.data.suggestions.isNotEmpty()) {
                return ApiResult.Success(r.data.suggestions)
            }
        }
        return ApiResult.Success(stubRecommendationSets[turnIndex % stubRecommendationSets.size])
    }

    // 미션 완료 — 실서버 POST /missions/{missionId}/complete (2026-07-22 실호출 검증).
    // 서버가 XP 지급·대화 종료까지 처리(xpEarned 응답, /xp/summary 실제 증가 확인).
    // 레벨업 연출용 before/after는 완료 전·후 /xp/summary 두 번 조회로 구성.
    // 체크리스트는 서버 미제공 — 로컬 문구 유지. 세션 없음/실패 시 기존 로컬 XP 경로 폴백.
    suspend fun completeMission(missionId: String, durationSec: Long = 0): ApiResult<MissionCompleteResult> {
        statusOverrides[missionId] = "완료" // 목록·저장 목록의 상태 필터에 바로 반영
        val cid = activeConversationId
        if (cid != null) {
            val before = serverCall { missionApi.getXpSummary() }
            val done = serverCall {
                missionApi.completeMission(
                    missionId,
                    MissionCompleteRequest(
                        conversationId = cid,
                        result = "success",
                        durationMinutes = ((durationSec + 59) / 60).toInt().coerceAtLeast(1),
                    ),
                )
            }
            if (done is ApiResult.Success) {
                activeConversationId = null // complete가 대화 종료를 겸함(실측: 이후 finish 불가)
                val after = serverCall { missionApi.getXpSummary() }
                val beforeLevel = (before as? ApiResult.Success)?.data?.level ?: userXpStore.level
                val beforeXp = (before as? ApiResult.Success)?.data?.currentXp ?: userXpStore.currentXp
                val afterLevel = (after as? ApiResult.Success)?.data?.level ?: beforeLevel
                val afterXp = (after as? ApiResult.Success)?.data?.currentXp
                    ?: (beforeXp + done.data.xpEarned)
                val nextXp = (after as? ApiResult.Success)?.data?.nextLevelXp ?: userXpStore.nextLevelXp
                // 홈 카드도 서버와 같은 숫자를 보게 로컬 스토어 동기화
                userXpStore.syncFromServer(afterLevel, afterXp, nextXp)
                return ApiResult.Success(
                    MissionCompleteResult(
                        checklist = stubCompleteChecklist,
                        gainedXp = done.data.xpEarned,
                        levelBefore = beforeLevel,
                        levelAfter = afterLevel,
                        xpBefore = beforeXp,
                        xpAfter = afterXp,
                        nextLevelXp = nextXp,
                    ),
                )
            }
        }
        // ── 폴백: 서버 세션 없음(오프라인 진입 등) — 기존 로컬 XP 경로 ──
        val gained = stubMissions.firstOrNull { it.id == missionId }?.rewardXp ?: 20
        val beforeXp = userXpStore.currentXp
        val beforeLevel = userXpStore.level
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

    // AI 피드백 조회 (E101). 응답은 피드백당 1세트(항목별 아님) — 문구는 UI CSS 목업 그대로
    // (4개 상세 프레임 내용 동일, 사용자 결정 2026-07-11: 길이 검증용 자작 4종 → 원문 환원).
    // TODO(서버 연동): 백엔드 피드백 API 미구현(이슈도 없음) — 이슈 생성 요청 상태.
    //     stub은 missionId를 feedbackId로 받음.
    suspend fun getFeedback(feedbackId: String): ApiResult<FeedbackResult> {
        return ApiResult.Success(
            FeedbackResult(
                // stub은 missionId를 feedbackId로 받으므로 그 미션의 제목을 그대로 씀.
                // (서버 연동 시엔 피드백 응답에 담겨 오는 미션 제목으로 교체)
                missionTitle = stubMissions.firstOrNull { it.id == feedbackId }?.title
                    ?: stubMissions.first().title,
                nickname = "소다123", // TODO(서버 연동): 유저 프로필 닉네임으로 교체
                kindnessScore = 92,
                initiativeScore = 88,
                empathyScore = 85,
                questionLinkScore = 78,
                strengths = listOf(
                    "상대를 존중하는 표현을 사용했어요",
                    "대화를 따뜻하게 시작했어요",
                    "긍정적인 말투를 유지했어요",
                ),
                improvements = listOf(
                    "조금 더 구체적인 칭찬을 해보세요",
                    "상대의 감정을 확인하는 표현을 사용해보세요",
                ),
                savedPhrase = "그렇군요! 저도 생각보다 편해서 놀랐어요",
                itemTexts = stubItemTexts,
            ),
        )
    }
}

// 항목별 피드백 문구 stub — 서버(AI)가 줄 값. 서버 연동 시 통째 삭제.
// 4개 상세 화면이 전부 같은 문구면 줄바꿈·자간·말줄임이 드러나지 않아, 항목마다 길이와
// 기호(따옴표·물음표·슬래시)를 달리해 레이아웃을 검증할 수 있게 해둠.
// 친절한 태도 = UI CSS 목업 원문 그대로.
internal val stubItemTexts = mapOf(
    "친절한 태도" to FeedbackItemText(
        strengths = listOf(
            "상대를 존중하는 표현을 사용했어요",
            "대화를 따뜻하게 시작했어요",
            "긍정적인 말투를 유지했어요",
        ),
        improvements = listOf(
            "조금 더 구체적인 칭찬을 해보세요",
            "상대의 감정을 확인하는 표현을 사용해보세요",
        ),
        savedPhrase = "그렇군요! 저도 생각보다 편해서 놀랐어요",
    ),
    // 긴 문장 — 불릿 줄바꿈, 저장 시트 카드의 말줄임 확인용
    "대화 주도" to FeedbackItemText(
        strengths = listOf(
            "대화가 끊길 만한 순간마다 먼저 새로운 이야깃거리를 꺼내며 흐름을 이어갔어요",
            "상대의 답변을 받아 자연스럽게 다음 화제로 넘어갔어요",
            "대화의 속도를 상대에게 맞춰 조절했어요",
        ),
        improvements = listOf(
            "상대가 충분히 말할 수 있도록 잠깐의 여백을 남겨보세요",
            "화제를 바꿀 때 한마디로 이어 붙여보세요",
        ),
        savedPhrase = "자주는 아니지만 가끔 이런 자리에 오면 기분이 좋아지더라고요",
    ),
    // 짧은 문장 — 줄 간격·불릿 정렬 확인용
    "공감 능력" to FeedbackItemText(
        strengths = listOf(
            "상대의 말에 맞장구를 쳤어요",
            "기분을 헤아리며 답했어요",
            "편안한 분위기를 만들었어요",
        ),
        improvements = listOf(
            "감정을 한 번 더 짚어주세요",
            "상대의 말을 요약해 되돌려주세요",
        ),
        savedPhrase = "아, 정말 그러셨겠어요",
    ),
    // 따옴표·물음표·슬래시 포함 — 자간과 기호 렌더 확인용
    "질문 연결성" to FeedbackItemText(
        strengths = listOf(
            "\"왜\"와 \"어떻게\"를 섞어 물어봤어요",
            "상대의 답에서 단어를 골라 이어 물었어요",
            "궁금해하는 마음이 자연스럽게 드러났어요",
        ),
        improvements = listOf(
            "예/아니오로 끝나는 질문을 조금 줄여보세요",
            "상대의 마지막 말을 따라 한 번 더 물어보세요",
        ),
        savedPhrase = "그때 기분이 어떠셨는지 여쭤봐도 될까요?",
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
// id는 서버(UUID)와 같은 String 타입 — stub은 "1".."7"로 단순화.
// 1~5번 = 피그마 목업 카드 그대로. 6번 = 목업에 잘려 보이는 카드(일상 대화) 추정. 7번 = 긴 제목(2줄 확장 검증용).
private val stubMissions = listOf(
    MissionListItem(
        id = "1",
        title = "처음 보는 사람에게 짧게 인사하기",
        category = "짧은 대화",
        difficulty = "쉬움",
        estimatedMinutes = 2,
        rewardXp = 20,
    ),
    MissionListItem(
        id = "2",
        title = "최근 본 영화 이야기하기",
        category = "짧은 대화",
        difficulty = "쉬움",
        estimatedMinutes = 5,
        rewardXp = 20,
        isSaved = true,
        status = "완료",
    ),
    MissionListItem(
        id = "3",
        title = "학교 생활 꿀팁 나누기",
        category = "일상 대화",
        difficulty = "보통",
        estimatedMinutes = 8,
        rewardXp = 30,
        isSaved = true,
        status = "진행중",
    ),
    MissionListItem(
        id = "4",
        title = "나의 취미를 소개해보기",
        category = "친구 만들기",
        difficulty = "어려움",
        estimatedMinutes = 10,
        rewardXp = 40,
    ),
    MissionListItem(
        id = "5",
        title = "주말 계획 이야기하기",
        category = "짧은 대화",
        difficulty = "쉬움",
        estimatedMinutes = 5,
        rewardXp = 20,
    ),
    MissionListItem(
        id = "6",
        title = "회사 생활 이야기하기",
        category = "일상 대화",
        difficulty = "보통",
        estimatedMinutes = 8,
        rewardXp = 30,
    ),
    MissionListItem(
        id = "7",
        title = "동아리에서 관심사가 비슷한 사람에게 먼저 말 걸어보기",
        category = "친구 만들기",
        difficulty = "어려움",
        estimatedMinutes = 15,
        rewardXp = 60,
    ),
)

// 미션 상세 효과 문구 stub. 1번 = 피그마 목업 문구 그대로. 3번 = 3개(개수 가변 검증용).
private val stubBenefits = mapOf(
    "1" to listOf(
        "낯선 사람과의 첫 대화에 자신감이 생겨요",
        "자연스럽게 대화를 이어갈 수 있어요",
    ),
    "3" to listOf(
        "일상 속 이야깃거리를 찾는 눈이 생겨요",
        "상대방과 공감대를 만들 수 있어요",
        "대화를 오래 이어가는 힘이 생겨요",
    ),
)

private val defaultBenefits = listOf(
    "대화에 자신감이 생겨요",
    "자연스럽게 대화를 이어갈 수 있어요",
)
