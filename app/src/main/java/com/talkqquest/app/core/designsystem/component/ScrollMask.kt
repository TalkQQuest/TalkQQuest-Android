package com.talkqquest.app.core.designsystem.component

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import com.talkqquest.app.core.designsystem.Gray50

// UI_Z.css 실측: 스크롤 마스크 그라데이션은 상자 높이 기준 130.71%에서야 알파가 0에
// 닿는다(31.41% / 81.06%에서 꺾이는 3토막 직선의 끝점이 상자 밖에 있다).
// CSS 높이 그대로 잘라서 그리면 페이드가 끝나는 쪽 알파가 0.28에서 뚝 끊겨
// 가로선으로 보인다(사용자 신고: "선은 얇게라도 보이면 안돼").
// → 실제로 그리는 높이는 CSS 높이 x 1.3071(=130.71%)까지 늘린다.
//   짙은 쪽 끝을 원래 CSS 위치에 맞추면 보이는 구간은 CSS와 완전히 같고,
//   나머지가 0까지 이어져 경계선이 사라진다.
const val ScrollMaskOverdraw = 1.3071f
fun scrollMaskHeight(cssHeight: Dp): Dp = cssHeight * ScrollMaskOverdraw

// CSS 원본은 0~31.41% 알파 0.8 평평 / 31.41~81.06% 0.8→0.45 / 81.06~130.71%(연장분) 0.45→0
// 인 3토막 직선이다. 이대로 옮기면 꺾이는 지점이 마흐 밴드(경계선처럼 보이는 현상)를 만든다.
// 그래서 양 끝 알파(0.8 / 0)는 CSS 그대로 두고, 사이를 smoothstep(3p²-2p³)으로 잇는다.
// 꺾이는 점도 평평한 구간도 없어진다. 11스텝이면 계단이 눈에 보이지 않는다.
// (실기기에서 검증돼 사용자 승인된 방식 — 새로 발명하지 않고 그대로 공용화한다.)
private fun smoothstepAlpha(p: Float): Float {
    val s = 3f * p * p - 2f * p * p * p
    return 0.8f * (1f - s)
}

// 위가 짙고(0.8) 아래로 갈수록 투명(0) — "위 흐림 막"에 쓴다.
fun topScrollMaskBrush(): Brush {
    val stops = Array(11) { i ->
        val p = i / 10f
        p to Gray50.copy(alpha = smoothstepAlpha(p))
    }
    return Brush.verticalGradient(*stops)
}

// 위가 투명(0) 아래로 갈수록 짙음(0.8) — top을 세로로 뒤집은 모양. "아래 흐림 막"에 쓴다.
// CSS도 아래 막을 transform: matrix(1,0,0,-1,0,0)(세로 반전)으로 이 모양을 만든다.
fun bottomScrollMaskBrush(): Brush {
    val stops = Array(11) { i ->
        val p = i / 10f
        p to Gray50.copy(alpha = smoothstepAlpha(1f - p))
    }
    return Brush.verticalGradient(*stops)
}
