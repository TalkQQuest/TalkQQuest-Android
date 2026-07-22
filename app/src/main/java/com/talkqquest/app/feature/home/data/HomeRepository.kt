package com.talkqquest.app.feature.home.data

import com.talkqquest.app.core.datastore.UserXpStore
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.core.network.safeApiCall
import com.talkqquest.app.feature.home.data.model.HomeSummary
import com.talkqquest.app.feature.home.data.model.TodayMission
import com.talkqquest.app.feature.mission.data.MissionApi
import javax.inject.Inject

// 홈 Repository (예시). ViewModel과 API 사이를 잇는 계층.
// 각 기능은 이 패턴대로 자기 Repository를 만들면 됩니다.
// - API를 safeApiCall로 감싸 ApiResult(성공/실패/예외)로 변환해 ViewModel에 넘김.
// - @Inject constructor: Hilt가 HomeApi를 자동으로 넣어줌(HomeModule에서 제공).
class HomeRepository @Inject constructor(
    private val homeApi: HomeApi,
    private val missionApi: MissionApi, // 오늘의 미션 카드 — 미션 API 재사용 (둘 다 B파트)
    private val userXpStore: UserXpStore, // 미션 완료 XP가 홈에도 보이게 공유 (서버 완료 후 sync됨)
) {
    // 프로필(닉네임·레벨·XP)= /users/me 실데이터, 오늘의 미션 = /missions/today 실데이터(폴백 있음),
    // 카운트·오늘의 질문 = 서버 미구현이라 stub 유지. 서버 실패(오프라인 등) 시 전부 stub 폴백 — 데모가 안 죽게.
    suspend fun getHomeSummary(): ApiResult<HomeSummary> {
        when (val me = safeApiCall { homeApi.getMe() }) {
            is ApiResult.Success -> {
                // 서버 레벨·XP로 1회 초기화 — 이후엔 미션 완료가 /xp/summary 값으로 sync해줌
                userXpStore.seedFromServer(me.data.level, me.data.xp)
                return ApiResult.Success(
                    stubHomeSummary.copy(
                        nickname = me.data.nickname ?: me.data.name, // 온보딩 전 nickname null → name
                        level = userXpStore.level,
                        currentXp = userXpStore.currentXp,
                        nextLevelXp = userXpStore.nextLevelXp,
                        todayMission = fetchTodayMission() ?: stubHomeSummary.todayMission,
                    ),
                )
            }
            else -> return ApiResult.Success( // 오프라인 폴백: 기존 stub 그대로
                stubHomeSummary.copy(
                    level = userXpStore.level,
                    currentXp = userXpStore.currentXp,
                    nextLevelXp = userXpStore.nextLevelXp,
                ),
            )
        }
    }

    // 오늘의 추천 미션 — 실서버 GET /missions/today.
    // ★실측(2026-07-22): 온보딩 미완료 계정은 MISSION_PROFILE_NOT_FOUND 에러
    //   → 미션 목록 첫 항목으로 폴백(실서버 UUID 유지 — 카드를 눌러도 실제 상세로 이어짐).
    //   카드 부제(description)는 목록 응답에 없어 상세 조회로 채움(실패 시 생략).
    private suspend fun fetchTodayMission(): TodayMission? {
        val item = when (val today = safeApiCall { missionApi.getTodayMission() }) {
            is ApiResult.Success -> today.data
            else -> {
                val list = safeApiCall { missionApi.getMissions() }
                (list as? ApiResult.Success)?.data?.missions?.firstOrNull() ?: return null
            }
        }
        val description = (safeApiCall { missionApi.getMissionDetail(item.id) } as? ApiResult.Success)
            ?.data?.description?.takeIf { it.isNotBlank() }
        return TodayMission(
            id = item.id,
            title = item.title,
            description = description,
            difficulty = item.difficulty,
            estimatedMinutes = item.estimatedMinutes,
            rewardXp = item.rewardXp,
        )
    }
}

// 서버 없이 에뮬에서 홈 화면 확인용 임시 데이터. 서버 연동 시 이 값째로 삭제.
private val stubHomeSummary = HomeSummary(
    nickname = "다민",
    level = 2,
    currentXp = 30,
    nextLevelXp = 100,
    todayMission = TodayMission(
        id = "1",
        title = "처음 보는 사람에게 짧게 인사하기",
        description = "가벼운 인사로 좋은 대화의 시작을 열어보세요!",
        difficulty = "쉬움",
        estimatedMinutes = 5,
        rewardXp = 20,
    ),
    archiveCount = 12,
    communityCount = 4,
    questionOfDay = "요즘 가장 설렜던 순간은?",
    hasNewNotification = false, // stub: 기본은 알림 없음 벨 (점 붙은 벨 확인하려면 true로)
)
