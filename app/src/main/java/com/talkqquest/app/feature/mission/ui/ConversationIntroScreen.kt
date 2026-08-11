package com.talkqquest.app.feature.mission.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.component.TqLoadingScreen

// ── 대화 진입 애니메이션 (CSS UI-15 "대화 진입 애니메이션" 프레임 전사) ──
// 대화를 준비하는 동안 잠깐 머무는 화면.
//
// 화면 틀·스피너·문구 위치는 전부 공용 TqLoadingScreen에 있다. 시안에서 이 화면과
// "온보딩->홈 애니메이션"(A담당)이 같은 프레임을 쓰고 문구만 다르기 때문이다.
// 여기서는 이 화면의 문구와 뒤로가기만 넘긴다.

@Composable
fun ConversationIntroScreen(onBack: () -> Unit = {}) {
    // CSS 문구 블록 167x80 — 높이 80 ÷ 행간 40 = 2줄.
    // 블록 폭 167이 "대화를 시작할"(6글자+공백)의 폭과 맞아떨어져(28x6 - 자간 ≈ 165)
    // 줄바꿈 자리가 거기로 확정된다. 자동 줄바꿈에 맡기지 않고 그대로 적는다.
    TqLoadingScreen(
        message = "대화를 시작할\n준비가 됐어요",
        onBack = onBack,
    )
}

@Preview(name = "대화 진입 애니메이션", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ConversationIntroPreview() {
    TalkQQuestTheme { ConversationIntroScreen() }
}
