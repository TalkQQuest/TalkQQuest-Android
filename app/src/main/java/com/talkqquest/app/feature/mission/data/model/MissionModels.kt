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
    val status: String = "미완료", // 진행 상태: 완료/진행중/미완료 (저장 목록 필터용. TODO 서버 필드·값 확정 시 조정)
)

// 미션 상세 DTO. 기능명세서 C103 기준 — GET /api/v1/missions/{missionId}
// 명세서에 응답 필드가 안 적혀 있어 목록 필드 + 효과 문구(benefits)로 시작. 백엔드 확정 시 조정.
@Serializable
data class MissionDetail(
    val id: Long,
    val title: String,
    val category: String,
    val difficulty: String,
    val estimatedMinutes: Int,
    val rewardXp: Int,
    val isSaved: Boolean = false,
    val benefits: List<String> = emptyList(), // 체크(✓) 효과 문구 — 개수 가변(디자인은 2개 예시)
) {
    // 저장 시트(MissionSaveSheet)가 목록 카드 형태를 쓰기 때문에 변환용.
    fun toListItem() = MissionListItem(id, title, category, difficulty, estimatedMinutes, rewardXp, isSaved)
}

// 대화 준비(미션 진입) DTO. 기능명세서 C103 — GET /api/v1/missions/{missionId}/prep
// 명세서에 응답 필드가 안 적혀 있어, 화면 구성 기준으로 추천 주제 + 첫 마디 문장으로 시작. 백엔드 확정 시 조정.
@Serializable
data class ConversationPrep(
    val topics: List<String> = emptyList(),   // 추천 주제 칩 (표시용) — 개수 가변(디자인은 6개)
    val openers: List<String> = emptyList(),  // 바로 쓰는 첫 마디 (복사 대상) — 개수 가변(디자인은 3개)
)
