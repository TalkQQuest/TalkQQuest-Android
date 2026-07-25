package com.talkqquest.app.core.network

import kotlinx.serialization.Serializable

// GET /api/v1/archives (보관함 목록) 응답 DTO. type 파라미터로 문장/리포트/미션/대화를 갈라 받는다.
// 문장 저장 시트(B·미션)와 리포트 저장 시트(B·리포트)가 같은 계약을 쓰므로, 한쪽 기능 패키지에 두어
// 기능끼리 얽히지 않게 공용 위치(ApiResponse 옆)에 둔다. C의 ArchiveApi에는 의존하지 않는다.
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
    val title: String = "",          // 문장 원문 / 리포트 제목
    val isBookmarked: Boolean = true,
    val createdAt: String = "",      // ISO8601 — 화면에선 yyyy.MM.dd로 변환
)
