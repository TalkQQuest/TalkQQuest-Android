package com.talkqquest.app.feature.home.data

import com.talkqquest.app.feature.home.data.model.HomeSummary
import javax.inject.Inject
import javax.inject.Singleton

// 스플래시에서 미리 받아둔 홈 요약을 잠깐 담아두는 단일 캐시.
// 목적: 스플래시가 세션 확인(getMe)을 기다리는 동안 홈 요약도 병렬로 받아두면,
//       홈에 들어갔을 때 두 번째 로딩 스피너 없이 바로 내용이 뜬다(체감 대기 단축).
// 인증 흐름은 그대로 — 라우팅은 여전히 세션 확인 결과가 결정한다.
@Singleton
class HomeSummaryCache @Inject constructor() {
    @Volatile
    private var value: HomeSummary? = null
    @Volatile
    private var loadedAt: Long = 0L

    fun put(summary: HomeSummary) {
        value = summary
        loadedAt = System.currentTimeMillis()
    }

    // 갓 받아온(기본 5초 이내) 캐시만 반환 — 오래된 값으로 낡은 화면을 보여주지 않게.
    fun takeFresh(maxAgeMillis: Long = 5_000L): HomeSummary? {
        val v = value ?: return null
        return if (System.currentTimeMillis() - loadedAt <= maxAgeMillis) v else null
    }
}
