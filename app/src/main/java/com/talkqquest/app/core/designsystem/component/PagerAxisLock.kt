package com.talkqquest.app.core.designsystem.component

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

// 가로 페이저(탭)와 세로 스크롤이 한 화면에 겹칠 때 쓰는 축 잠금.
//
// 문제: 세로로 밀려는데 손가락이 살짝 대각선으로 흐르면 페이저가 먼저 잡아채 옆 탭으로 넘어간다.
// 해결: 한 제스처의 첫 움직임이 세로 우세면 그 손가락을 놓을 때까지 페이저를 잠근다.
//
// 판정은 Initial 패스에서 한다. 페이저의 드래그 처리(Main 패스)보다 앞서 실행돼야
// 페이저가 손가락을 붙잡기 전에 잠글 수 있다. 아무것도 consume하지 않으므로
// 자식(세로 리스트) 스크롤과 정상적인 가로 스와이프는 그대로 동작한다.
//
// 쓰는 곳: 하단 네비 4탭(MainTabsPager), 보관함 4탭, 획득한 뱃지 3탭, 회원가입 약관 2탭.
// 페이저를 감싸는 컨테이너에 modifier로 붙이고, 반환된 값을 HorizontalPager의
// userScrollEnabled에 `!locked`로 넣는다.
class PagerAxisLockState internal constructor() {
    var isVerticalDrag by mutableStateOf(false)
        internal set
}

@Composable
fun rememberPagerAxisLockState(): PagerAxisLockState = remember { PagerAxisLockState() }

fun Modifier.pagerAxisLock(state: PagerAxisLockState): Modifier = this.pointerInput(Unit) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        // 페이저 슬롭의 절반 지점에서 미리 판정해, 페이저가 옆으로 움직이기 전에 잠근다.
        val decideAt = viewConfiguration.touchSlop * 0.5f
        var decided = false
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val change = event.changes.firstOrNull { it.id == down.id }
            if (change == null || !change.pressed) break
            if (!decided) {
                val dx = kotlin.math.abs(change.position.x - down.position.x)
                val dy = kotlin.math.abs(change.position.y - down.position.y)
                if (dx > decideAt || dy > decideAt) {
                    state.isVerticalDrag = dy > dx
                    decided = true
                }
            }
        }
        state.isVerticalDrag = false
    }
}
