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
    // TODO(서버 연동 전 임시): 백엔드 홈 API 붙으면 아래 stub 리턴 지우고 return 한 줄로 복구.
    //     suspend fun getHomeSummary() = safeApiCall { homeApi.getHomeSummary() }
    suspend fun getHomeSummary(): ApiResult<HomeSummary> =
        ApiResult.Success(
            stubHomeSummary.copy(
                level = userXpStore.level,
                currentXp = userXpStore.currentXp,
                nextLevelXp = userXpStore.nextLevelXp,
            ),
        )
}

// 서버 없이 에뮬에서 홈 화면 확인용 임시 데이터. 서버 연동 시 이 값째로 삭제.
private val stubHomeSummary = HomeSummary(
    nickname = "다민",
    level = 2,
    currentXp = 30,
    nextLevelXp = 100,
    todayMission = TodayMission(
        id = 1,
        title = "처음 보는 사람에게 짧게 인사하기",
        description = "가벼운 인사로 좋은 대화의 시작을 열어보세요!",
        difficulty = "쉬움",
        estimatedMinutes = 5,
        rewardXp = 20,
    ),
    archiveCount = 12,
    communityCount = 4,
    questionOfDay = "요즘 가장 설렜던 순간은?",
    hasNewNotification = true, // stub: 점 붙은 벨이 데모에서 보이게
)
