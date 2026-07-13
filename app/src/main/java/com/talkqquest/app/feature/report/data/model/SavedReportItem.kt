package com.talkqquest.app.feature.report.data.model

// 저장(북마크)된 리포트 한 건 — 리포트 저장 시트·보관함(리포트) 카드에 표시.
// TODO(서버 연동): 리포트 아카이브 API(E102) 응답 모델로 교체. 서버 명세 확정 전 데모용.
data class SavedReportItem(
    val id: String, // 서버 리포트 id = UUID 문자열

    val title: String,
    val savedDate: String, // 저장 날짜 "2026.08.20" (서버 오면 저장 시각을 yyyy.MM.dd로)
    val isSaved: Boolean = true,
)
