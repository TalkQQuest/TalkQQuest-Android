package com.talkqquest.app.feature.home.data.model

import kotlinx.serialization.Serializable

// 홈 요약(C101) DTO. 기능명세서 기준. 실제 백엔드와 다르면 필드 조정.
// GET /api/v1/home/summary 의 응답(data) 형태.
@Serializable
data class HomeSummary(
    val nickname: String,
    val level: Int,
    val currentXp: Int,
    val nextLevelXp: Int,
    val todayMission: TodayMission? = null,
    val archiveCount: Int,
    val communityCount: Int,
    val questionOfDay: String? = null,
    val hasNewNotification: Boolean = false, // 알림 있음 — 벨에 점 표시 (서버 몫, 백엔드 필드명 확정 시 조정)
)

// 내 프로필 — 백엔드 GET /api/v1/users/me 응답 data와 1:1 (user-profile.dto).
// 홈 인사말(닉네임)·레벨·XP의 실데이터 소스.
@Serializable
data class UserMe(
    val id: String = "",
    val name: String = "",
    val nickname: String? = null, // 온보딩 전엔 null — 그동안은 name으로 표시
    val avatarUrl: String? = null,
    val bio: String? = null,
    val level: Int = 1,
    val xp: Int = 0,
    val dailyConversationGoal: Int = 1,
    val onboardingCompleted: Boolean = false,
)

// 오늘의 미션 요약 (홈 카드용). 중첩 객체 예시.
@Serializable
data class TodayMission(
    val id: String, // 서버 미션 id = UUID 문자열

    val title: String,
    val description: String? = null,  // 미션 한 줄 설명 (홈 카드 부제). 백엔드 필드명 확정 시 조정.
    val difficulty: String,       // 예: 쉬움/보통/어려움 (실제 값은 백엔드 확인)
    val estimatedMinutes: Int,
    val rewardXp: Int,
)
