package com.talkqquest.app.core.util

import kotlinx.serialization.Serializable

// 능력치 누적 원값 4개 (스웨거 GrowthMetricTotalsDto). GET /reports/growth와 GET /home/summary가
// 똑같이 내려주고 성장 리포트·홈 레벨 카드가 함께 쓰므로 core에 둔다.
// 가입 이후 누적 총점이라 값이 줄지 않는다(차감 없음).
// 실서버 실측(2026-08-13): 필드명이 `kindness`가 아니라 `kindnessTotal` 형태다.
@Serializable
data class GrowthTotalsDto(
    val kindnessTotal: Int = 0,
    val initiativeTotal: Int = 0,
    val empathyTotal: Int = 0,
    val questionLinkTotal: Int = 0,
)

// 성장 티어 계산 — 서버는 능력치 누적 원값 4개(growthTotals)만 주고, 마름모·별·티어는 앱이 계산한다.
//  - 축당 300점. 4축이 "모두" 300을 채워야 마름모 1개 완성 = 별 1개
//  - 초과분은 버리지 않고 다음 마름모로 이월 → 완성 개수는 가장 느린 축이 결정한다
//  - 별 3개를 채우면 티어 승급. 티어에 갓 올라오면 별 0개
// 홈 레벨 카드의 티어 줄과 성장 리포트가 같은 값을 보여야 해서 core에 둔다(둘 다 이 함수만 쓴다).
object TierProgress {

    const val AXIS_MAX = 300      // 축당 만점 = 별 1개에 필요한 축별 점수
    const val STARS_PER_TIER = 3  // 티어 하나당 마름모(별) 3개

    // 마스터는 단계 없는 종착이라 그 위로 승급 대상이 없다.
    // 이름 문자열이 곧 휘장 리소스 키다(tierEmblemRes) — "다이아몬드"가 아니라 "다이아".
    val TIERS = listOf("브론즈", "실버", "골드", "플래티넘", "다이아", "마스터")

    data class Result(
        val tierName: String,
        val tierStars: Int,        // 현재 티어에서 채운 별 0..3
        val nextTierName: String,  // 마스터면 빈 문자열
        val nextStarsNeeded: Int,  // 마스터면 0
        val axisScores: List<Int>, // 친절·주도·공감·질문 순, 진행 중인 마름모의 축별 점수 0..300
        val diamonds: Int,         // 지금까지 완성한 마름모 총개수 — 직전 상태와 비교해 승급을 판정한다
    )

    fun of(kindness: Int, initiative: Int, empathy: Int, questionLink: Int): Result {
        val totals = listOf(kindness, initiative, empathy, questionLink).map { it.coerceAtLeast(0) }
        // 완성한 마름모 수 = 가장 느린 축이 300을 몇 번 넘겼는가.
        val diamonds = totals.minOf { it / AXIS_MAX }
        val tierIndex = (diamonds / STARS_PER_TIER).coerceAtMost(TIERS.lastIndex)
        val isMaster = tierIndex == TIERS.lastIndex
        val starsInTier = diamonds % STARS_PER_TIER
        // 완성한 마름모가 이미 먹은 점수. 남은 값이 지금 그리는 마름모의 축별 점수다.
        val consumed = diamonds * AXIS_MAX
        return Result(
            tierName = TIERS[tierIndex],
            // 마스터는 더 채울 단계가 없어 별 3개를 채운 상태로 둔다.
            tierStars = if (isMaster) STARS_PER_TIER else starsInTier,
            nextTierName = if (isMaster) "" else TIERS[tierIndex + 1],
            nextStarsNeeded = if (isMaster) 0 else STARS_PER_TIER - starsInTier,
            // 이월분이 300을 넘어도 차트는 300에서 멈춘다(넘겨 그리면 도형이 깨진다).
            axisScores = totals.map { (it - consumed).coerceIn(0, AXIS_MAX) },
            diamonds = diamonds,
        )
    }
}
