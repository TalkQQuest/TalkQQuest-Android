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
)

// 오늘의 미션 요약 (홈 카드용). 중첩 객체 예시.
@Serializable
data class TodayMission(
    val id: Long,
    val title: String,
    val difficulty: String,       // 예: 쉬움/보통/어려움 (실제 값은 백엔드 확인)
    val estimatedMinutes: Int,
    val rewardXp: Int,
)
