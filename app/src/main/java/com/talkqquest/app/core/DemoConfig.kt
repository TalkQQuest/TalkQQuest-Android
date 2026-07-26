package com.talkqquest.app.core

// 데모용 목업 스위치.
// true  = B 화면들(홈·미션·대화·완료 등)이 서버 대신 목업(stub)을 표시.
// false = 실서버 API 연동으로 동작(원래 상태).
// ※ 서버 연동 코드는 그대로 유지 — 이 값 하나만 바꾸면 목업↔서버 전환됨.
object DemoConfig {
    const val USE_MOCK = true
}
