package com.talkqquest.app.feature.home.data

import com.talkqquest.app.core.datastore.UserXpStore
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.core.network.safeApiCall
import com.talkqquest.app.feature.home.data.model.HomeSummary
import com.talkqquest.app.feature.home.data.model.TodayMission
import javax.inject.Inject

// 홈 Repository (예시). ViewModel과 API 사이를 잇는 계층.
// 각 기능은 이 패턴대로 자기 Repository를 만들면 됩니다.
// - API를 safeApiCall로 감싸 ApiResult(성공/실패/예외)로 변환해 ViewModel에 넘김.
// - @Inject constructor: Hilt가 HomeApi를 자동으로 넣어줌(HomeModule에서 제공).
class HomeRepository @Inject constructor(
    private val homeApi: HomeApi,
    private val userXpStore: UserXpStore, // 서버 전 임시: 미션 완료 XP가 홈에도 보이게 공유
) {
    // 프로필(닉네임·레벨·XP)은 서버 /users/me 실데이터, 나머지(오늘의 미션·카운트)는
    // 서버 미구현이라 stub 유지. 서버 실패(오프라인 등) 시엔 전부 stub 폴백 — 데모가 안 죽게.
    // TODO(서버 연동): 홈 대시보드·오늘의 미션 API 생기면 stub 부분 교체.
    suspend fun getHomeSummary(): ApiResult<HomeSummary> {
        when (val me = safeApiCall { homeApi.getMe() }) {
            is ApiResult.Success -> {
                // 서버 레벨·XP로 1회 초기화 — 이후 미션 완료 가산은 로컬이 이어감
                userXpStore.seedFromServer(me.data.level, me.data.xp)
                return ApiResult.Success(
                    stubHomeSummary.copy(
                        nickname = me.data.nickname ?: me.data.name, // 온보딩 전 nickname null → name
                        level = userXpStore.level,
                        currentXp = userXpStore.currentXp,
                        nextLevelXp = userXpStore.nextLevelXp,
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
