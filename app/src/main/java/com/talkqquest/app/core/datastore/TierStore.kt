package com.talkqquest.app.core.datastore

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// 유저 티어(등급) 공유 상태. UserXpStore(레벨/XP)와 같은 이유로 존재한다.
// 홈이 그리는 티어는 HomeUiState.summary(서버 재조회 응답)에만 담겨 있어, 성장 리포트에서
// 승급을 본 뒤 홈으로 돌아오면 재조회가 끝나기 전까지 옛 티어가 한 프레임 스친다.
// 티어 값을 아는 쪽(홈 요약 조회·성장 리포트 조회)이 여기에 써 두면, 홈은 이 저장소를 구독해
// 복귀 첫 프레임부터 최신 티어를 그릴 수 있다. @Singleton: 앱에 1개.
data class TierSnapshot(val tierName: String, val tierStars: Int)

@Singleton
class TierStore @Inject constructor() {

    // 초기값은 null = "아직 아는 값 없음". 홈이 서버 응답을 한 번도 못 받은 상태에서
    // 엉뚱한 기본 티어(예: 브론즈)를 먼저 그리면 안 되므로, 값이 들어오기 전까지는 아무 것도 주지 않는다.
    private val _tier = MutableStateFlow<TierSnapshot?>(null)
    val tier: StateFlow<TierSnapshot?> = _tier.asStateFlow()

    fun update(tierName: String, tierStars: Int) {
        if (tierName.isBlank()) return
        _tier.value = TierSnapshot(tierName, tierStars)
    }
}
