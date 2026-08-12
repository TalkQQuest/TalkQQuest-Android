package com.talkqquest.app.feature.home.data

import com.talkqquest.app.core.datastore.UserXpStore
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.core.network.serverCall
import com.talkqquest.app.core.util.TierProgress
import com.talkqquest.app.feature.home.data.model.HomeSummary
import com.talkqquest.app.feature.home.data.model.TodayMission
import com.talkqquest.app.feature.mission.data.MissionApi
import com.talkqquest.app.feature.notification.data.NotificationRepository
import javax.inject.Inject

// 홈 Repository (예시). ViewModel과 API 사이를 잇는 계층.
// 각 기능은 이 패턴대로 자기 Repository를 만들면 됩니다.
// - API를 safeApiCall로 감싸 ApiResult(성공/실패/예외)로 변환해 ViewModel에 넘김.
// - @Inject constructor: Hilt가 HomeApi를 자동으로 넣어줌(HomeModule에서 제공).
class HomeRepository @Inject constructor(
    private val homeApi: HomeApi,
    private val missionApi: MissionApi, // 오늘의 미션 카드 — 미션 API 재사용 (둘 다 B파트)
    private val notificationRepository: NotificationRepository, // 알림창과 홈 종이 같은 읽음 상태를 공유
    private val userXpStore: UserXpStore, // 미션 완료 XP가 홈에도 보이게 공유 (서버 완료 후 sync됨)
) {
    // 홈 요약 — GET /api/v1/home/summary (dev 배포 기준 구현됨): 닉네임·레벨·XP·카운트·오늘의 질문을 한 번에.
    // 오늘의 미션이 응답에 없거나 null이면 /missions/today로 폴백. XP는 완료 가산 보존 위해 1회만 seed하고 이후 로컬 유지.
    // 서버 실패(오프라인·데모 USE_MOCK) 시 전부 stub 폴백 — 데모가 안 죽게.
    suspend fun getHomeSummary(): ApiResult<HomeSummary> {
        when (val res = serverCall { homeApi.getHomeSummary() }) {
            is ApiResult.Success -> {
                val d = res.data
                // 서버 레벨·XP로 1회 초기화 — 이후엔 미션 완료가 값을 이어감.
                // nextLevelXp까지 넘겨야 진행바 분모가 서버와 같아진다(레벨마다 필요량이 달라짐).
                userXpStore.seedFromServer(d.level, d.currentXp, d.nextLevelXp)
                // 실전 티어는 서버가 이름/별을 주지 않는다 — 누적 원값(growthTotals)으로 앱이 계산한다.
                // 성장 리포트와 같은 TierProgress를 써야 두 화면의 티어·별이 어긋나지 않는다.
                val tier = TierProgress.of(
                    kindness = d.growthTotals.kindnessTotal,
                    initiative = d.growthTotals.initiativeTotal,
                    empathy = d.growthTotals.empathyTotal,
                    questionLink = d.growthTotals.questionLinkTotal,
                )
                return ApiResult.Success(
                    d.copy(
                        tierName = tier.tierName,
                        tierStars = tier.tierStars,
                        // 닉네임 미설정 계정(null→"") 폴백: /users/me의 nickname→name → 그래도 없으면 stub 문구
                        nickname = d.nickname.ifBlank { fallbackNickname() },
                        level = userXpStore.level,
                        currentXp = userXpStore.currentXp,
                        nextLevelXp = userXpStore.nextLevelXp,
                        todayMission = d.todayMission ?: fetchTodayMission() ?: stubHomeSummary.todayMission,
                        questionOfDay = d.questionOfDay ?: stubHomeSummary.questionOfDay,
                        // /home/summary엔 알림 필드가 없어(실측) 알림 API로 별도 계산
                        hasNewNotification = hasUnreadNotification(),
                    ),
                )
            }
            else -> return ApiResult.Success( // 오프라인/데모 폴백: 기존 stub 그대로
                stubHomeSummary.copy(
                    level = userXpStore.level,
                    currentXp = userXpStore.currentXp,
                    nextLevelXp = userXpStore.nextLevelXp,
                ),
            )
        }
    }

    // 닉네임 미설정 계정용 폴백 — GET /users/me의 nickname→name 순. 둘 다 없으면 stub 문구 유지.
    private suspend fun fallbackNickname(): String {
        val me = (serverCall { homeApi.getMe() } as? ApiResult.Success)?.data
        return me?.nickname?.takeIf { it.isNotBlank() }
            ?: me?.name?.takeIf { it.isNotBlank() }
            ?: stubHomeSummary.nickname
    }

    // 벨 보라 점 — 서버의 안읽음 목록에서 알림창이 방금 읽은 항목을 제외하고 하나라도 남았는지 확인.
    // 실패/데모(USE_MOCK)면 false = 기본 벨 (알림창의 목업 폴백과 무관하게 점은 실데이터만 신뢰).
    private suspend fun hasUnreadNotification(): Boolean = notificationRepository.hasUnreadNotification()

    // 오늘의 추천 미션 — 실서버 GET /missions/today.
    // ★실측(2026-07-22): 온보딩 미완료 계정은 MISSION_PROFILE_NOT_FOUND 에러
    //   → 미션 목록 첫 항목으로 폴백(실서버 UUID 유지 — 카드를 눌러도 실제 상세로 이어짐).
    //   카드 부제(description)는 목록 응답에 없어 상세 조회로 채움(실패 시 생략).
    // ★2026-08-11: 오늘의 미션 응답에 description이 함께 오므로 상세를 한 번 더 부르지 않는다.
    //   재추천 횟수를 다 쓴 계정은 429(MISSION_REFRESH_LIMIT_EXCEEDED)가 오는데, 이때도 화면이
    //   비지 않도록 기존처럼 목록 첫 항목으로 폴백한다(실서버 UUID라 카드를 눌러도 상세로 이어짐).
    private suspend fun fetchTodayMission(): TodayMission? {
        when (val today = serverCall { missionApi.getTodayMission() }) {
            is ApiResult.Success -> {
                val d = today.data
                if (d.missionId.isNotBlank()) {
                    return TodayMission(
                        id = d.missionId,
                        title = d.title,
                        description = d.description.takeIf { it.isNotBlank() },
                        difficulty = d.difficulty,
                        estimatedMinutes = d.estimatedMinutes,
                        rewardXp = d.rewardXp,
                    )
                }
            }
            else -> Unit
        }
        // 폴백: 목록 첫 항목 (부제는 목록 응답에 없어 상세로 채움 — 실패하면 생략)
        val list = serverCall { missionApi.getMissions() }
        val item = (list as? ApiResult.Success)?.data?.missions?.firstOrNull() ?: return null
        val description = (serverCall { missionApi.getMissionDetail(item.id) } as? ApiResult.Success)
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
    nickname = "소다123",
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
