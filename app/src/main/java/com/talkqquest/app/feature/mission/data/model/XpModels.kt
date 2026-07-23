package com.talkqquest.app.feature.mission.data.model

import kotlinx.serialization.Serializable

// GET /api/v1/xp/summary 응답 data — 2026-07-22 실서버 실측 그대로.
// 미션 완료 화면의 레벨/XP 바 계산에 사용 (완료 전·후 두 번 조회해 before/after 구성).
@Serializable
data class XpSummary(
    val level: Int = 1,
    val currentXp: Int = 0,
    val nextLevelXp: Int = 100,
    val totalXp: Int = 0,
)
