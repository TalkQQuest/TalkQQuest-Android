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
    fun seedFromServer(serverLevel: Int, serverXp: Int) {
        if (seeded) return
        seeded = true
        level = serverLevel
        currentXp = serverXp
    }

    // 미션 완료 보상 반영. 실제 레벨업 규칙(필요 XP 증가 등)은 서버 정책이지만,
    // stub도 100을 넘으면 초과분 이월+레벨업 해서 데모에서 숫자가 깨지지 않게 함(110/100 방지).
    fun addXp(gained: Int) {
        currentXp += gained
        while (currentXp >= nextLevelXp) {
            currentXp -= nextLevelXp
            level++
        }
    }
}
