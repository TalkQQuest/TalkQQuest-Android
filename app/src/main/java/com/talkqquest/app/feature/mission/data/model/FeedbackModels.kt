package com.talkqquest.app.feature.mission.data.model

// AI 피드백 결과 (기능명세서 E101 응답 필드명 그대로: kindnessScore, initiativeScore,
// empathyScore, questionLinkScore, strengths[], improvements[], savedPhrase).
// 항목 4종은 명세에 필드로 고정 — 값들만 서버 몫. 잘한 점/개선할 점/베스트 문장은
// 명세·목업 모두 피드백당 1세트(항목별 아님 — 4프레임 내용 동일)라 이 구조로 확정(사용자 결정).
data class FeedbackResult(
    val missionTitle: String,   // 이 피드백이 나온 미션 제목 — 리포트 저장 카드의 제목으로 쓰임
    val nickname: String,       // "다민님을 위한..." 슬롯 — 서버(유저 프로필) 몫, stub "다민"
    val kindnessScore: Int,     // 친절한 태도
    val initiativeScore: Int,   // 대화 주도
    val empathyScore: Int,      // 공감 능력
    val questionLinkScore: Int, // 질문 연결성
    val strengths: List<String>,     // 잘한 점 불릿
    val improvements: List<String>,  // 개선할 점 불릿
    val savedPhrase: String,         // 베스트 문장
    // 항목별 문구(항목명 → 문구). 비어 있으면 위 공통 값을 4개 항목이 함께 씀.
    // 명세(E101)는 피드백당 1세트지만, 상세 4화면이 전부 같은 문구면 어색해 항목별로 다를 수 있게 열어둠.
    // TODO(서버 연동): 항목별 세트를 주는지 백엔드와 확인 — 지금은 stub이 채움.
    val itemTexts: Map<String, FeedbackItemText> = emptyMap(),
)

// 상세 화면 한 항목의 문구 묶음 (잘한 점 / 개선할 점 / 베스트 문장)
data class FeedbackItemText(
    val strengths: List<String>,
    val improvements: List<String>,
    val savedPhrase: String,
)

// 해당 항목의 문구 — 항목별 값이 없으면 피드백 공통 값으로 폴백
fun FeedbackResult.textFor(label: String): FeedbackItemText =
    itemTexts[label] ?: FeedbackItemText(strengths, improvements, savedPhrase)

// 요약 카드 행·상세 배너가 함께 쓰는 항목 목록 (이름은 명세 고정, 순서 = 디자인 카드 순서)
fun FeedbackResult.scoreItems(): List<Pair<String, Int>> = listOf(
    "친절한 태도" to kindnessScore,
    "대화 주도" to initiativeScore,
    "공감 능력" to empathyScore,
    "질문 연결성" to questionLinkScore,
)
