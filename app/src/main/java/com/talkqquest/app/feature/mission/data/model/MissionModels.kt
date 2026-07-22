package com.talkqquest.app.feature.mission.data.model

import kotlinx.serialization.Serializable

// 미션 목록 카드 1개 DTO — 백엔드 mission.dto.ts MissionListItemDto와 1:1.
// id는 서버가 UUID 문자열(Char(36))이라 String.
@Serializable
data class MissionListItem(
    val id: String,
    val title: String,
    val category: String,       // 예: 짧은 대화 / 친구 만들기 / 학교생활
    val difficulty: String,     // 서버 enum: 쉬움 / 보통 / 어려움
    val estimatedMinutes: Int,
    val rewardXp: Int,
    val isSaved: Boolean = false, // 북마크 여부
    // 진행 상태(완료/진행중/미완료) — 서버 목록 응답엔 없는 로컬 필드 (저장 목록 필터용).
    // TODO(백엔드): 저장 목록에 상태 필터가 필요하면 서버 필드 요청.
    val status: String = "미완료",
)

// GET /api/v1/missions 응답 data — 백엔드 MissionListResponseDto { missions, pageInfo }.
@Serializable
data class MissionListResponse(
    val missions: List<MissionListItem> = emptyList(),
    val pageInfo: MissionPageInfo = MissionPageInfo(),
)

@Serializable
data class MissionPageInfo(
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalCount: Int = 0,
)

// 미션 상세 DTO — 백엔드 MissionDetailResponseDto와 1:1.
// benefits(체크 ✓ 효과 문구)는 서버 응답에 없는 로컬 필드 — 화면(효과/보상 카드)이 쓰는 중.
// TODO(백엔드): 효과 문구를 서버에서 줄지 확인. 당장은 stub이 채움.
@Serializable
data class MissionDetail(
    val id: String,
    val title: String,
    val category: String,
    val difficulty: String,
    val estimatedMinutes: Int,
    val rewardXp: Int,
    val description: String = "",
    val preparationTip: String? = null,
    val caution: String? = null,
    val isSaved: Boolean = false,
    val benefits: List<String> = emptyList(), // 로컬 전용(위 주석 참고) — 개수 가변(디자인은 2개)
) {
    // 저장 시트(MissionSaveSheet)가 목록 카드 형태를 쓰기 때문에 변환용.
    fun toListItem() = MissionListItem(id, title, category, difficulty, estimatedMinutes, rewardXp, isSaved)
}

// GET /api/v1/missions/{missionId}/prep 응답 data — 백엔드 MissionPrepResponseDto와 1:1.
// type: question(추천 주제) / starter(첫 마디) / tip(팁 — 현재 화면 슬롯 없음)
@Serializable
data class MissionPrepResponse(
    val missionId: String = "",
    val totalCount: Int = 0,
    val items: List<MissionPrepItem> = emptyList(),
) {
    // 화면용 변환: question → 주제 칩, starter → 첫 마디 문장 (orderIndex 순 정렬)
    fun toConversationPrep() = ConversationPrep(
        topics = items.filter { it.type == "question" }.sortedBy { it.orderIndex }.map { it.content },
        openers = items.filter { it.type == "starter" }.sortedBy { it.orderIndex }.map { it.content },
    )
}

@Serializable
data class MissionPrepItem(
    val id: String,
    val type: String, // question | starter | tip
    val content: String,
    val orderIndex: Int = 0,
)

// 대화 준비 화면용 모델 (화면 구성 그대로: 추천 주제 + 첫 마디).
// 서버 응답(MissionPrepResponse)을 toConversationPrep()으로 변환해 사용.
@Serializable
data class ConversationPrep(
    val topics: List<String> = emptyList(),   // 추천 주제 칩 (표시용) — 개수 가변(디자인은 6개)
    val openers: List<String> = emptyList(),  // 바로 쓰는 첫 마디 (복사 대상) — 개수 가변(디자인은 3개)
)

// POST /api/v1/missions/{missionId}/save 응답 data — 백엔드 MissionSaveResponseDto.
@Serializable
data class MissionSaveResponse(
    val missionId: String = "",
    val isSaved: Boolean = true,
    val savedAt: String? = null,
)

// POST /api/v1/missions/{missionId}/complete 요청 body — 백엔드 CompleteConversationRequestDto.
// result: success | failure | avoidance
// ★result에 기본값 금지: Json encodeDefaults=false라 기본값 필드는 요청에서 빠짐
//   (대화 mode/role이 이걸로 서버 500 났던 것과 같은 함정 — 2026-07-22 실측).
@Serializable
data class MissionCompleteRequest(
    val conversationId: String,
    val result: String, // 호출부에서 항상 명시
    val memo: String? = null,
    val durationMinutes: Int,
    val emotion: String? = null,
)

// 완료 응답 data — 백엔드 CompleteConversationResponseDto.
// 레벨 계산은 서버가 안 주므로(xpEarned만) 당분간 클라(UserXpStore)에서 계산.
@Serializable
data class MissionCompleteResponse(
    val missionRecordId: String = "",
    val status: String = "completed",
    val xpEarned: Int = 0,
    val completedAt: String = "",
)
