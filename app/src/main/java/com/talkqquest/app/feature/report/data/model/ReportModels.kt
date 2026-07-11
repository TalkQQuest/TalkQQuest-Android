package com.talkqquest.app.feature.report.data.model

// 리포트 화면 모델 (명세 E102 — 응답 필드가 명세에 없어 화면(UI CSS) 기준으로 정의.
// TODO(서버 연동): GET /api/v1/reports/monthly · weekly-compare 응답 확정되면 필드 맞춤)

// 성장 리포트 탭
data class GrowthReport(
    val prevLevel: Int,                     // 저번 주 레벨
    val currentLevel: Int,                  // 이번 주 레벨
    val growthPercent: Int,                 // 대화 성장 추이 우측 "+ 18%"
    val weekLabels: List<String>,           // 차트 x축 라벨 ("7월 4주" …)
    val categoryRanks: List<CategoryRank>,  // 카테고리 TOP (많이 한 순)
    val completedMissions: Int,             // 완료한 미션 수
    val totalMissions: Int,                 // 전체 미션 수
)

data class CategoryRank(
    val name: String,  // 카테고리 이름 (여행/음식/…)
    val count: Int,    // 완료 횟수
)

// 주간 비교 리포트 탭
data class WeeklyCompareReport(
    val metrics: List<MetricChange>,     // 핵심 지표 4종 (E101 항목 순서 고정)
    val highlights: List<HighlightItem>, // 개선 하이라이트 문구
)

// 하이라이트 문구: 앞 키워드만 보라 강조 (피그마 렌더 — 나머지는 검정).
// TODO(서버 연동): 응답이 통문장으로 오면 키워드 분리 방식 협의해 조정.
data class HighlightItem(
    val emphasis: String,  // 보라 강조 부분 ("전체 점수" 등 — 조사 제외)
    val rest: String,      // 나머지 문장
)

data class MetricChange(
    val name: String,   // 지표 이름 (친절한 태도/대화 주도/…)
    val lastWeek: Int,  // 지난주 점수
    val thisWeek: Int,  // 이번 주 점수
)
