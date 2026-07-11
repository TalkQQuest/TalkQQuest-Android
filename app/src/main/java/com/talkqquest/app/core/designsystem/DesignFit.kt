package com.talkqquest.app.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

// ── 작은 화면 대응: 디자인 통째 축소 (사용자 결정) ──
// 피그마 기준 화면(393x852)보다 작은 기기에선 밀도를 낮춰 글자·카드·이미지·여백을
// 전부 같은 비율로 줄임 → 배치 그대로 한 화면에 담김.
//
// 축소율 = min( 가로/393,  (세로 - 140) / 712,  1 )
//  · 140 = 축소돼도 물리 크기가 고정되는 몫: 시스템 네비 인셋(48) + 하단 네비 알약 블록(92 —
//    화면들이 92/scale 로 물리 크기를 확보함). 화면 = 852·배율 + 140·(1-배율) ≤ 세로 를 풀면 위 식.
//  · 393x852 기기 = (852-140)/712 = 정확히 1.0 (피그마와 픽셀 일치),
//    작은 기기는 알약·시스템 네비의 축소 불가분까지 계산에 넣어 안 넘침 (예: 360x740 → 0.843).
//  · (구) 세로/900 근사는 852 기기도 5% 줄였고, 인셋만 뺀 중간 공식(764 기준)은 알약 몫을
//    빠뜨려 알약 화면(대화 준비 등)이 작은 기기에서 넘쳤음 — 그 이력 끝에 확정한 식 (2026-07-11).
//
// 상태바 보정: 실제 상태바가 디자인 상태바(40, 축소 반영)보다 낮으면 부족분을 위 여백으로 채워
// 콘텐츠 시작 y를 피그마와 일치시킴 (각 화면의 statusBarsPadding 위에 얹힘 — 전 화면 루트 배경이
// Gray50이라 여백 띠는 티 안 남).

// 현재 적용된 축소 비율 (1 = 원본). 화면 밖 요소(하단 알약)와 맞닿는 여백 보정용.
val LocalDesignScale = staticCompositionLocalOf { 1f }

@Composable
fun FitDesign(
    compensateStatusBar: Boolean = true, // false = 중첩 사용(팝업 등)에서 상태바 보정 이중 적용 방지
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val base = LocalDensity.current
        val statusInset = with(base) { WindowInsets.statusBars.getTop(this).toDp() }
        val raw = minOf(
            maxWidth / 393.dp,
            (maxHeight - 140.dp) / 712.dp,
            1f,
        )
        // 키보드가 창을 리사이즈하는 기기에선 키보드가 뜰 때 maxHeight가 줄어 축소율이 폭락
        // (채팅 화면이 콩알만 해짐) → 키보드 없는 상태의 축소율을 잠가두고 IME 중엔 그 값 유지.
        val imeOpen = WindowInsets.ime.getBottom(base) > 0
        var lockedScale by remember { mutableFloatStateOf(raw) }
        if (!imeOpen && lockedScale != raw) lockedScale = raw // 회전 등 실제 크기 변화만 반영
        val scale = if (imeOpen) lockedScale else raw

        // 디자인 상태바(40 x 축소율)보다 실제 상태바가 낮은 만큼 위 여백으로 보충
        val statusShortfall =
            if (compensateStatusBar) (40.dp * scale - statusInset).coerceAtLeast(0.dp) else 0.dp
        Box(modifier = Modifier.fillMaxSize().padding(top = statusShortfall)) {
            if (scale < 1f) {
                CompositionLocalProvider(
                    LocalDensity provides Density(base.density * scale, base.fontScale),
                    LocalDesignScale provides scale,
                ) {
                    content()
                }
            } else {
                content()
            }
        }
    }
}
