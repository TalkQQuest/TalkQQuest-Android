package com.talkqquest.app.feature.mission.data.model

import kotlinx.serialization.Serializable

// POST /api/v1/archives/phrases (문장 저장) 요청/응답 DTO.
// dev 백엔드 실계약(archive.dto.ts createPhraseRequestSchema/CreatePhraseResponseDto) 대조 — 2026-07-25.
// 저장되는 곳은 아카이브 '문장' 도메인(표시는 C 담당)이지만, "저장" 호출은 AI 피드백 상세(B)에서 발생하므로
// 호출 DTO/엔드포인트는 B 패키지에 둔다. (C의 ArchiveApi는 건드리지 않음)
@Serializable
data class CreatePhraseRequest(
    val conversationId: String, // 이 문장이 나온 대화 id (UUID) — 서버 필수
    val content: String,        // 저장할 문장 (서버: trim 후 1자 이상)
    val memo: String? = null,   // 선택 메모
)

@Serializable
data class CreatePhraseResponse(
    val id: String,
    val conversationId: String = "",
    val content: String = "",
    val memo: String? = null,
    val createdAt: String = "",
)

// DELETE /api/v1/archives/phrases/{phraseId} (저장 해제)
@Serializable
data class DeleteArchiveItemResponse(
    val itemId: String = "",
    val deleted: Boolean = false,
)

// GET /api/v1/archives?type=phrase (저장한 문장 목록) 응답 DTO — 문장 저장 시트의 "최근 저장한 문장"용.
// 위 저장/삭제 DTO와 같은 이유로 B 패키지에 둔다(C의 ArchiveApi 의존 없이 우리 코드만으로 동작).
// 서버 응답엔 필드가 더 있지만 Json(ignoreUnknownKeys = true)이라 쓰는 것만 선언한다.
@Serializable
data class ArchiveListResponse(
    val totalCount: Int = 0,
    val items: List<ArchiveListItem> = emptyList(),
)

@Serializable
data class ArchiveListItem(
    val id: String = "",
    val referenceId: String? = null, // 상세 조회에 쓰는 id(문장이면 phraseId) — 없으면 id로 폴백
    val title: String = "",          // 문장 원문
    val isBookmarked: Boolean = true,
    val createdAt: String = "",      // ISO8601 — 화면에선 yyyy.MM.dd로 변환
)
