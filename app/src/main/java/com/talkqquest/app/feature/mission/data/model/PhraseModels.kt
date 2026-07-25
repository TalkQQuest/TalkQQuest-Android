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
