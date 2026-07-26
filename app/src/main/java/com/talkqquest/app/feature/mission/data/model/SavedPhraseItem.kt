package com.talkqquest.app.feature.mission.data.model

// 저장(북마크)된 베스트 문장 한 건 — 문장 저장 시트·보관함(문장) 카드에 표시.
// GET /archives?type=phrase 응답(ArchiveListItem)을 Repository에서 이 모델로 변환해 채운다.
data class SavedPhraseItem(
    val id: String, // 서버(Saved_Phrases)가 UUID 문자열이라 String

    val phrase: String,    // 저장한 문장 원문 (카드에선 1줄 말줄임)
    val savedDate: String, // 저장 날짜 "2026.08.20" (서버 오면 저장 시각을 yyyy.MM.dd로)
    val isSaved: Boolean = true,
)
