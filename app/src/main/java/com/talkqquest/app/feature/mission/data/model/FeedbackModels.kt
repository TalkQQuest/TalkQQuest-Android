package com.talkqquest.app.feature.mission.data.model

import kotlinx.serialization.Serializable

// AI 피드백 결과 (기능명세서 E101 응답 필드명 그대로: kindnessScore, initiativeScore,
// empathyScore, questionLinkScore, strengths[], improvements[], savedPhrase).
// 항목 4종은 명세에 필드로 고정 — 값들만 서버 몫. 잘한 점/개선할 점/베스트 문장은
// 명세·목업 모두 피드백당 1세트(항목별 아님 — 4프레임 내용 동일)라 이 구조로 확정(사용자 결정).
data class FeedbackResult(
    // 이 피드백의 대화 id — 문장 저장 API(POST /archives/phrases)에 필수.
    // 서버 응답 FeedbackDetailResponseDto.conversationId에서 채워짐. stub/미연동 시 null → 저장은 화면 표시만.
    val conversationId: String? = null,
    val missionTitle: String,   // 이 피드백이 나온 미션 제목 — 리포트 저장 카드의 제목으로 쓰임
    val nickname: String,       // "소다123님을 위한..." 슬롯 — 서버(유저 프로필) 몫, stub "소다123"
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

// POST /api/v1/feedback (피드백 생성) — dev CreateFeedbackRequestDto/FeedbackResponseDto 대조(2026-07-25).
// 대화 종료 후 이 대화에 대한 피드백 생성을 트리거. 생성은 비동기(status pending → ready).
@Serializable
data class CreateFeedbackRequest(val conversationId: String)

@Serializable
data class CreateFeedbackResponse(
    val feedbackId: String = "",
    val status: String = "",   // pending | ready | failed
)

// ── 서버 DTO — GET /api/v1/feedback/{feedbackId} (dev FeedbackDetailResponseDto 대조, 2026-07-25) ──
// metrics는 항상 4개(kindness/initiative/empathy/questionLink 순서 고정), 각 지표에 점수+잘한점+개선점+베스트문장.
@Serializable
data class FeedbackMetricDto(
    val key: String = "",          // kindness | initiative | empathy | questionLink
    val label: String = "",
    val score: Int = 0,
    val strengths: List<String> = emptyList(),
    val improvements: List<String> = emptyList(),
    val bestSentence: String? = null,
)

@Serializable
data class FeedbackDetailResponse(
    val id: String = "",
    val conversationId: String = "",
    val topic: String? = null,     // 대화 주제 — 서버 응답엔 미션 제목이 따로 없어 이걸 제목 슬롯에 사용
    val overallScore: Int = 0,
    val metrics: List<FeedbackMetricDto> = emptyList(),
    val missionSummary: List<String> = emptyList(),
    val savedPhrase: String? = null,
    val status: String = "",       // pending | ready | failed
    val createdAt: String = "",
)

// 서버 지표 key → 앱 라벨 (scoreItems 라벨과 일치해야 itemTexts가 상세 배너에 매칭됨)
private fun metricLabelKo(key: String): String = when (key) {
    "kindness" -> "친절한 태도"
    "initiative" -> "대화 주도"
    "empathy" -> "공감 능력"
    "questionLink" -> "질문 연결성"
    else -> key
}

// 서버 응답 → 화면 모델. missionTitle/nickname은 피드백 응답에 없어 호출부가 넘김
// (TODO: missionTitle은 서버 피드백에 없음 — topic으로 대체 중, nickname은 프로필 몫).
fun FeedbackDetailResponse.toFeedbackResult(missionTitle: String, nickname: String): FeedbackResult {
    fun scoreOf(k: String) = metrics.firstOrNull { it.key == k }?.score ?: 0
    val itemTexts = metrics.associate { m ->
        metricLabelKo(m.key) to FeedbackItemText(
            strengths = m.strengths,
            improvements = m.improvements,
            savedPhrase = m.bestSentence ?: savedPhrase.orEmpty(),
        )
    }
    val first = metrics.firstOrNull()
    return FeedbackResult(
        conversationId = conversationId,
        missionTitle = missionTitle,
        nickname = nickname,
        kindnessScore = scoreOf("kindness"),
        initiativeScore = scoreOf("initiative"),
        empathyScore = scoreOf("empathy"),
        questionLinkScore = scoreOf("questionLink"),
        strengths = first?.strengths ?: emptyList(),
        improvements = first?.improvements ?: emptyList(),
        savedPhrase = savedPhrase ?: first?.bestSentence ?: "",
        itemTexts = itemTexts,
    )
}
