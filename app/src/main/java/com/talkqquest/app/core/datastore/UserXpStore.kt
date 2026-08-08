package com.talkqquest.app.core.datastore

import javax.inject.Inject
import javax.inject.Singleton

// 유저 레벨/XP 공유 상태 (서버 연동 전 임시).
// 홈 요약 카드와 미션 완료 화면이 화면(ViewModel)마다 stub을 복사해 들면 서로 안 맞음
// (미션을 완료해도 홈이 30/100 그대로) → 북마크(savedOverrides)와 같은 이유로 한 곳에 모아
// 모든 화면이 같은 값을 봄. @Singleton: 앱에 1개.
// TODO(서버 연동): 서버가 유저 XP를 관리하므로 이 클래스 삭제하고 각 API 응답 값 사용.
@Singleton
class UserXpStore @Inject constructor() {

    // 시작값 = 홈 목업(Lv.2, 30/100XP). 서버 프로필이 오면 seedFromServer가 실제 값으로 교체.
    var level: Int = 2
        private set
    var currentXp: Int = 30
        private set
    var nextLevelXp: Int = 100
        private set

    private var seeded = false

    // 서버 프로필(/users/me)의 level·xp로 1회 초기화 — 이후 미션 완료 가산은 로컬(addXp)이 이어감.
    // (완료 API가 서버 XP를 갱신하기 전까지의 임시 동기화. 서버 연동 완료 시 클래스째 삭제)
    // ★serverNextLevelXp를 반드시 함께 넘길 것: 빠뜨리면 초기값 100이 남아 화면에 "250/100XP"처럼
    //   분모만 낡은 값이 표시된다(2026-07-28 실제 발생 — 홈 첫 진입에서만 어긋났음).
    fun seedFromServer(serverLevel: Int, serverXp: Int, serverNextLevelXp: Int? = null) {
        if (seeded) return
        seeded = true
        level = serverLevel
        currentXp = serverXp
        nextLevelXp = serverNextLevelXp?.takeIf { it > 0 } ?: requiredXpFor(serverLevel)
    }

    // 서버 완료 처리 후 /xp/summary 값으로 강제 동기화 — 홈 카드가 서버와 같은 숫자를 보게.
    // (seed와 달리 매번 덮어씀. 서버가 XP의 단일 진실이 된 실연동 경로에서 사용)
    fun syncFromServer(serverLevel: Int, serverXp: Int, serverNextLevelXp: Int) {
        seeded = true
        level = serverLevel
        currentXp = serverXp
        nextLevelXp = serverNextLevelXp
    }

    // 미션 완료 보상 반영 — 서버 실패(오프라인) 시에만 타는 폴백 경로.
    // 레벨업할 때마다 필요 XP를 다시 계산한다: 서버 mission-completion.service.ts와 동일한 절차라야
    // 온라인 복귀 후 syncFromServer가 덮어쓸 때 숫자가 튀지 않는다.
    fun addXp(gained: Int) {
        currentXp += gained
        while (currentXp >= nextLevelXp) {
            currentXp -= nextLevelXp
            level++
            nextLevelXp = requiredXpFor(level)
        }
    }

    companion object {
        // 서버 xp/services/level.service.ts의 calculateNextLevelXp와 같은 공식.
        // 서버는 응답에 nextLevelXp를 담아 주므로 온라인에서는 그 값이 우선이고,
        // 이 공식은 값을 못 받은 오프라인 폴백에서만 쓴다.
        // ★서버 쪽은 "레벨업 필요 XP 미확정 — level * 100으로 가정" 상태(TODO 주석)라
        //   기획이 확정되면 서버 공식과 함께 이 함수도 같이 고쳐야 한다.
        fun requiredXpFor(level: Int): Int = (level.coerceAtLeast(1)) * 100
    }
}
