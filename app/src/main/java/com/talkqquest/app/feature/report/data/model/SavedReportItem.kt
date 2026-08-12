package com.talkqquest.app.feature.report.data.model

// 저장(북마크)된 리포트 한 건 — 리포트 저장 시트·보관함(리포트) 카드에 표시.
// GET /archives?type=report 응답(ArchiveListItem)을 Repository에서 이 모델로 변환해 채운다.
data class SavedReportItem(
    val id: String, // 서버 리포트 id = UUID 문자열

    val title: String,
    val savedDate: String, // 저장 날짜 "2026.08.20" (서버 오면 저장 시각을 yyyy.MM.dd로)
    val isSaved: Boolean = true,
    // growth | weekly_compare — 화면엔 안 쓰고, 북마크를 껐다 켤 때 같은 종류로 재저장하는 데 씀.
    val type: String = "",
)
