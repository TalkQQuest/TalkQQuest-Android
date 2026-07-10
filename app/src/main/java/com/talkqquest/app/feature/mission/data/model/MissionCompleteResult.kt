package com.talkqquest.app.feature.mission.data.model

// 미션 완료 결과 (미션 완료&XP 화면). 전부 서버가 계산해 내려주는 값.
// TODO(서버 연동): 완료 API 응답 필드명 확정되면 맞춰 조정.
data class MissionCompleteResult(
    val checklist: List<String>, // 대화에서 잘한 점 체크리스트 (개수 가변)
    val gainedXp: Int,           // 이번 미션으로 얻은 XP (+20 XP)
    val levelBefore: Int,        // 획득 전 레벨 — levelAfter와 다르면 레벨업 연출
    val levelAfter: Int,         // 획득 후 레벨
    val xpBefore: Int,           // 획득 전 보유 XP (levelBefore 기준) — 바 카운트업 시작값
    val xpAfter: Int,            // 획득 후 보유 XP (levelAfter 기준)
    val nextLevelXp: Int,        // 다음 레벨 필요 XP (100)
)
