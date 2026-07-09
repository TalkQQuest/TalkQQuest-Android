package com.talkqquest.app.feature.mission.data

import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.core.network.safeApiCall
import com.talkqquest.app.feature.mission.data.model.ConversationPrep
import com.talkqquest.app.feature.mission.data.model.MissionDetail
import com.talkqquest.app.feature.mission.data.model.MissionListItem
import javax.inject.Inject

// 미션 Repository. ViewModel과 API 사이 계층 (홈 패턴과 동일).
class MissionRepository @Inject constructor(
    private val missionApi: MissionApi,
) {
    // TODO(서버 연동 전 임시): 백엔드 미션 API 붙으면 아래 stub 리턴 지우고 return 한 줄로 복구.
    //     suspend fun getMissions() = safeApiCall { missionApi.getMissions() }
    suspend fun getMissions(): ApiResult<List<MissionListItem>> =
        ApiResult.Success(stubMissions)

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
                isSaved = item.isSaved,
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
}

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
    ),
    MissionListItem(
        id = 3,
        title = "학교 생활 꿀팁 나누기",
        category = "일상 대화",
        difficulty = "보통",
        estimatedMinutes = 8,
        rewardXp = 30,
        isSaved = true,
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
