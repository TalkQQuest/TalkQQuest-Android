package com.talkqquest.app.feature.report.data.model

// ── 주간 비교 리포트(홈/알림창에서 진입) 화면 모델 ──
// UI 14차 "주간 비교 리포트(홈/알림창에서 진입)" 프레임 전사 기준.
// ReportScreen 안의 '주간 비교' 탭과 달리 주차 이동·자주 연습한 주제·미션 진행률이 함께 있는
// 독립 화면이라, 탭이 쓰는 WeeklyCompareReport와는 별도 모델로 둔다.
data class WeeklyCompareDetail(
    val prevWeekLabel: String,           // 왼쪽 주차 ("7월 4주차")
    val thisWeekLabel: String,           // 오른쪽 주차 ("8월 1주차")
    val metrics: List<MetricChange>,     // 핵심 지표 4종 (친절한 태도/대화 주도/공감 표현/질문 연결성)
    val highlights: List<String>,        // 개선 하이라이트 — 서버가 통문장으로 준다
    val topics: List<CategoryRank>,      // 자주 연습한 주제 (많이 한 순)
    val completedMissions: Int,          // 완료한 미션 수
    val totalMissions: Int,              // 전체 미션 수
) {
    // 도넛 진행률 (0~100). 전체가 0이면 0%로 — 나눗셈 방어.
    val progressPercent: Int
        get() = if (totalMissions <= 0) 0 else (completedMissions * 100) / totalMissions
}
