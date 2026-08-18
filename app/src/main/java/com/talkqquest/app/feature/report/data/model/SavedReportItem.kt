package com.talkqquest.app.feature.report.data.model

// 저장(북마크)된 리포트 한 건 — 리포트 저장 시트·보관함(리포트) 카드에 표시.
// GET /archives?type=report 응답(ArchiveListItem)을 Repository에서 이 모델로 변환해 채운다.
data class SavedReportItem(
    val id: String, // 서버 리포트 id = UUID 문자열

    val title: String,
    val savedDate: String, // 저장 날짜 "2026.08.20" (서버 오면 저장 시각을 yyyy.MM.dd로)
    val isSaved: Boolean = true,
    // 서버가 실제 리포트 번호를 내려줬는가. 저장 직후 임시 번호("0","1"…)를 든 카드만 false —
    // 그 사이에 카드를 누르면 없는 번호로 열려 실패하므로 화면이 이 값으로 이동을 미룬다.
    // 서버에서 불러온 목록은 이미 진짜 번호라 기본값 true로 지금처럼 바로 동작한다.
    val hasServerId: Boolean = true,
    // growth | weekly_compare — 화면엔 안 쓰고, 북마크를 껐다 켤 때 같은 종류로 재저장하는 데 씀.
    val type: String = "",
    // 주간 비교 리포트를 저장했을 때만 채운다. 둘 다 차 있으면 카드 제목이 미션명 대신
    // "7월 4주차 →(그림) 8월 1주차 주간 비교 리포트"가 되고, 화살표는 주간 비교 화면
    // 주차 줄과 같은 그림(ic_weekly_range_arrow)을 글자 사이에 끼워 그린다.
    val prevWeekLabel: String = "",
    val thisWeekLabel: String = "",
)
