package com.talkqquest.app.feature.mission.data.model

import kotlinx.serialization.Serializable

// 미션 목록 카드 1개 DTO. 기능명세서 C102 기준.
// GET /api/v1/missions 응답(data) 항목: id, title, category, difficulty, estimatedMinutes, rewardXp, isSaved
// difficulty 타입은 명세서에 미명시(홈과 동일하게 문자열 "쉬움/보통/어려움"로 시작) — 백엔드 확정 시 조정.
@Serializable
data class MissionListItem(
    val id: Long,
    val title: String,
    val category: String,       // 예: 짧은 대화 / 친구 만들기 / 학교생활
    val difficulty: String,     // 예: 쉬움 / 보통 / 어려움
    val estimatedMinutes: Int,
    val rewardXp: Int,
    val isSaved: Boolean = false, // 북마크 여부
)
