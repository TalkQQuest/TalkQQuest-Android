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

    // 마스터에 진입하는 데 필요한 마름모 수(= 마스터 앞 티어까지 전부 채운 개수).
    // 15를 직접 적지 않고 티어 개수·별 개수로 계산해, 상수 값이 바뀌어도 같이 따라가게 한다.
    val MASTER_ENTRY_DIAMONDS = TIERS.lastIndex * STARS_PER_TIER

    data class Result(
        val tierName: String,
        val tierStars: Int,        // 현재 티어에서 채운 별 0..3
        val nextTierName: String,  // 마스터면 빈 문자열
        val nextStarsNeeded: Int,  // 마스터에서 별 3개를 다 채우면 0
        val axisScores: List<Int>, // 친절·주도·공감·질문 순, 진행 중인 마름모의 축별 점수 0..300
        val diamonds: Int,         // 지금까지 완성한 마름모 총개수 — 직전 상태와 비교해 승급을 판정한다
        // 축이 이번 마름모에 필요한 300을 넘겨 벌어 둔 몫(0이면 넘긴 게 없다).
        // 마름모 완성은 가장 느린 축이 정하므로, 앞서 나간 축은 별을 받은 뒤에도 화면에서
        // 300에 멈춰 있는다. 그 이유를 유저가 알 수 없어 이 값을 문장으로 알려준다.
        val axisSurplus: List<Int>,
    ) {
        // 마스터에서 별 3개까지 채운 최종 상태. 더 올라갈 티어도 별도 없다.
        // 이 상태에서는 마름모·별·승급 연출을 재생하지 않는다(사용자 결정) — 마름모는 가득 찬 채
        // 고정되고 꼭짓점은 +0으로 표시된다. 마름모를 마지막으로 채우는 그 한 번만 예외로
        // 마름모가 차오르는 연출을 보여준다.
        val isMasterMaxed: Boolean
            get() = tierName == TIERS.last() && tierStars == STARS_PER_TIER
    }

    fun of(kindness: Int, initiative: Int, empathy: Int, questionLink: Int): Result {
        val totals = listOf(kindness, initiative, empathy, questionLink)
            .map { it.coerceAtLeast(0) }
        // 완성한 마름모 수 = 가장 느린 축이 300을 몇 번 넘겼는가.
        val diamonds = totals.minOf { it / AXIS_MAX }
        val tierIndex = (diamonds / STARS_PER_TIER).coerceAtMost(TIERS.lastIndex)
        val isMaster = tierIndex == TIERS.lastIndex
        // 마스터는 다른 티어와 똑같이 별 0개부터 시작해 3개에서 멈춘다.
        // diamonds % STARS_PER_TIER를 그대로 쓰면 마름모가 정확히 3의 배수(예: 18개)일 때
        // 나머지가 0이 되어 별이 도로 0개로 보이는 버그가 생기므로, 마스터 진입 마름모 수를
        // 뺀 값을 0..3으로 잘라 쓴다.
        val tierStars = if (isMaster) {
            (diamonds - MASTER_ENTRY_DIAMONDS).coerceIn(0, STARS_PER_TIER)
        } else {
            diamonds % STARS_PER_TIER
        }
        // 완성한 마름모가 이미 먹은 점수. 남은 값이 지금 그리는 마름모의 축별 점수다.
        val consumed = diamonds * AXIS_MAX
        // 만렙 지점(마름모 18칸) = 마스터 진입 마름모 수 + 마스터에서 채우는 별 3개, 축당 300점.
        // 초과분은 "다음 목표에 반영되는 몫"인데 만렙 너머로는 반영될 곳이 없으므로,
        // 그 축의 누적 점수(totals) 기준으로 만렙까지 남은 만큼으로 잘라야 한다.
        val maxAxisTotal = (MASTER_ENTRY_DIAMONDS + STARS_PER_TIER) * AXIS_MAX
        return Result(
            tierName = TIERS[tierIndex],
            tierStars = tierStars,
            nextTierName = if (isMaster) "" else TIERS[tierIndex + 1],
            nextStarsNeeded = STARS_PER_TIER - tierStars,
            axisScores = if (isMaster && tierStars == STARS_PER_TIER) {
                // 마스터 별 3개를 다 채운 뒤에는 마름모가 다시 차오르지 않고 가득 찬 상태로 고정한다.
                totals.map { AXIS_MAX }
            } else {
                // 이월분이 300을 넘어도 차트는 300에서 멈춘다(넘겨 그리면 도형이 깨진다).
                totals.map { (it - consumed).coerceIn(0, AXIS_MAX) }
            },
            diamonds = diamonds,
            // 이번 마름모 몫(300)까지 뺀 나머지가 "다음 목표에 반영되는" 몫이다.
            // 마스터 만렙은 마름모를 가득 찬 상태로 고정하므로 넘길 곳이 없어 0으로 둔다.
            axisSurplus = if (isMaster && tierStars == STARS_PER_TIER) {
                totals.map { 0 }
            } else {
                totals.map { total ->
                    val rawSurplus = (total - consumed - AXIS_MAX).coerceAtLeast(0)
                    // 만렙까지 그 축이 더 들어갈 수 있는 여유분. 이미 만렙 이상이면 0이라
                    // 그 축은 문장이 뜨지 않는다.
                    val roomToMaxLevel = (maxAxisTotal - total).coerceAtLeast(0)
                    minOf(rawSurplus, roomToMaxLevel)
                }
            },
        )
    }
}
