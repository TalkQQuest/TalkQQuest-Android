package com.talkqquest.app.feature.mission.data.model

// AI 피드백 결과 (기능명세서 E101 응답 필드명 그대로: kindnessScore, initiativeScore,
// empathyScore, questionLinkScore). 항목 4종은 명세에 필드로 고정 — 점수만 서버 몫.
// 응답의 strengths[]/improvements[]/savedPhrase는 상세 화면 몫 — 상세 구현 때 추가.
data class FeedbackResult(
    val kindnessScore: Int,     // 친절한 태도
    val initiativeScore: Int,   // 대화 주도
    val empathyScore: Int,      // 공감 능력
    val questionLinkScore: Int, // 질문 연결성
)
