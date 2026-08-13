package com.talkqquest.app.feature.mission.data.model

import kotlinx.serialization.Serializable

// 대화 메시지 1개 (화면용).
data class ChatMessage(
    val id: String, // 서버 메시지 id = UUID 문자열 (백엔드 ConversationMessage.id)

    val text: String,
    val isFromUser: Boolean, // true = 내 말풍선(보라, 오른쪽) / false = AI(흰색, 왼쪽)
    val time: String,        // 예: "9:20" — 같은 발신자 묶음의 마지막 말풍선 옆에만 표시
)

// ── 이하 서버 DTO — 2026-07-22 실서버 응답 실측 그대로 ──

// POST /api/v1/conversations 요청 body. selectedTopic은 옵션(없이도 생성됨 — 실측 확인).
// ★mode에 기본값 금지: Json이 encodeDefaults=false라 기본값 필드는 요청에서 빠지는데,
//   서버는 mode 없으면 SERVER_ERROR(500) (2026-07-22 실측 — 대화가 전부 stub 폴백되던 원인).
@Serializable
data class ConversationCreateRequest(
    val missionId: String,
    val mode: String, // text | voice — 호출부에서 항상 명시
    val selectedTopic: String? = null, // null 기본값은 의도적(빠지면 서버가 옵션 처리 — 실측 OK)
    // 대화 설정 4단계에서 저장한 준비 정보 id (POST /missions/{id}/setups 응답).
    // ★이걸 보내야 고른 장소·상대·친밀도·말투가 이 대화에 반영된다.
    //   설정만 저장하고 여기서 안 넘기면 저장은 되지만 대화에는 아무 영향이 없다.
    val missionSetupId: String? = null,
)

// POST /api/v1/conversations 응답 data.
@Serializable
data class ConversationCreateResponse(
    val conversationId: String,
    val missionId: String = "",
    val missionTitle: String = "",
    val mode: String = "text",
    val selectedTopic: String? = null,
    val status: String = "in_progress",
    val startedAt: String = "",
    // 첫 AI 인사말 — 2026-08-10 백엔드 추가. 예전엔 안 내려와서 앱이 자체 문구를 띄웠다.
    // 비어 오면(생성 실패 등) 기존 로컬 문구로 폴백한다.
    val openingMessage: String = "",
)

// POST /api/v1/conversations/{id}/messages 요청 body. role은 user 고정(서버 enum).
// ★role에 기본값 금지: 기본값이면 요청에서 빠져 서버 500 (위 mode와 같은 함정 — 실측).
@Serializable
data class ConversationMessageRequest(
    val role: String, // "user" — 호출부에서 항상 명시
    val content: String,
)

// 메시지 응답의 개별 메시지. role: user | guide (서버의 AI 응답 role은 "guide").
@Serializable
data class ServerChatMessage(
    val id: String = "",
    val role: String = "",
    val content: String = "",
    val createdAt: String = "",
)

// POST /api/v1/conversations/{id}/messages 응답 data — { userMessage, guideMessage }.
@Serializable
data class ConversationMessageResponse(
    val userMessage: ServerChatMessage? = null,
    val guideMessage: ServerChatMessage? = null,
)

// GET /api/v1/conversations/{id}/suggestions 응답 data — { suggestions: [문장…] }.
@Serializable
data class ConversationSuggestionsResponse(
    val suggestions: List<String> = emptyList(),
)

// ── 2026-08-13 추가 연동분 ──

// POST /api/v1/conversations/{id}/finish 요청 body.
// ★status에 기본값 금지 — 위 mode·role과 같은 함정(기본값이면 요청에서 빠진다).
@Serializable
data class ConversationFinishRequest(
    val status: String, // "completed" | "abandoned" — 호출부에서 항상 명시
)

@Serializable
data class ConversationFinishResponse(
    val conversationId: String = "",
    val status: String = "",
    val finishedAt: String = "",
)

// GET /api/v1/conversations/{id}/guide 응답 data.
// suggestions API와 겹치는 듯 보이지만 이쪽이 넓다 — 대화 가이드 카드가 함께 온다.
@Serializable
data class ConversationGuideResponse(
    val conversationId: String = "",
    val guideCards: List<String> = emptyList(),
    val suggestedReplies: List<String> = emptyList(),
)

// GET /api/v1/conversations/{id} 응답 data — 지난 대화를 통째로 받는다.
// status: in_progress | completed | abandoned.
@Serializable
data class ConversationDetailResponse(
    val conversationId: String = "",
    val missionId: String = "",
    val status: String = "",
    val startedAt: String = "",
    val finishedAt: String = "",
    val durationMinutes: Int = 0,
    val messages: List<ServerChatMessage> = emptyList(),
)
