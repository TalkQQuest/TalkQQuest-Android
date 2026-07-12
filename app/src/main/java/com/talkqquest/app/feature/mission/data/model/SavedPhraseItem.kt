package com.talkqquest.app.feature.mission.data.model

// 저장(북마크)된 베스트 문장 한 건 — 문장 저장 시트·보관함(문장) 카드에 표시.
// TODO(서버 연동): 문장 아카이브 API 응답 모델로 교체. 서버 명세 확정 전 데모용.
data class SavedPhraseItem(
    val id: Long,
    val phrase: String,    // 저장한 문장 원문 (카드에선 1줄 말줄임)
    val savedDate: String, // 저장 날짜 "2026.08.20" (서버 오면 저장 시각을 yyyy.MM.dd로)
    val isSaved: Boolean = true,
)
