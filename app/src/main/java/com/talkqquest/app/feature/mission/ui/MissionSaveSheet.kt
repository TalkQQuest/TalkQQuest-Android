package com.talkqquest.app.feature.mission.ui

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.Gray100
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.feature.mission.data.model.MissionListItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

// ── 저장(북마크) 바텀시트 (UI 1차 v2.css "미션 목록에서 북마크"/"북마크(끌어올린 상태)" 전사) ──
// 미션 목록·미션 상세 공용. 모달이 아님: 배경이 안 어두워지고 뒤 화면도 계속 만질 수 있음.
// 동작: 저장 직후 하단 333 높이로 살짝 올라옴(CSS) → 위로 끌면 화면 꼭대기 근처까지 펼쳐짐(끌어올린 상태, CSS top 68)
//       → 아래로 쓸어내리면 닫힘 → 안 만지고 두면 잠시 후 자동으로 스윽 내려감.

// 시트를 안 만지면 자동으로 닫히기까지의 시간.
private const val AUTO_DISMISS_MILLIS = 2_000L

// 시트가 살짝 올라온 높이 (CSS: 시트 top 519 → 화면 852 기준 보이는 부분 333)
private val SheetPeekHeight = 333.dp

// 끌어올렸을 때 시트 위 여백 (CSS top 68 = 목업 상태바 40 + 28 → 실기기는 상태바 인셋 + 28)
private val ExpandedTopGap = 28.dp

// 올라가는 시간. 내려갈 땐 2배 느리게 (사용자 결정).
private const val ASCENT_MILLIS = 300
private val AscentSpec: AnimationSpec<Float> = tween(ASCENT_MILLIS, easing = FastOutSlowInEasing)
private val DescentSpec: AnimationSpec<Float> = tween(ASCENT_MILLIS * 2, easing = FastOutSlowInEasing)

// 놓았을 때 이 속도(px/s)보다 빠르면 위치와 무관하게 그 방향의 다음 정착점으로.
private const val SETTLE_VELOCITY = 1_000f

private val SheetShape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)

// 화면 내용을 감싸서 저장 시트를 위에 겹쳐 그리는 틀.
// savedMission이 생기면 시트가 올라오고, null이 되면 내려감.
@Composable
internal fun MissionSaveSheetScaffold(
    savedMission: MissionListItem?,
    recentSavedMissions: List<MissionListItem>,
    onDismiss: () -> Unit,
    onMissionClick: (Long) -> Unit,
    onToggleSave: (Long) -> Unit,
    onSheetTopChange: (Float?) -> Unit = {}, // 시트 위 끝 y(px), null=시트 없음. 하단 네비가 이 선 아래를 안 그려 시트 뒤에 있던 것처럼 드러남
    onSavedListClick: () -> Unit = {}, // TODO: 저장 목록 화면(피그마 "북마크→저장목록") 생기면 연결
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        content()

        val density = LocalDensity.current
        // 시트 위 끝(offsetY)이 오갈 세 정착점(px): 숨김 = 화면 바닥 / 살짝 = 바닥에서 333 / 펼침 = 꼭대기 근처
        val hiddenOffset = constraints.maxHeight.toFloat()
        val peekOffset = hiddenOffset - with(density) { SheetPeekHeight.toPx() }
        val expandedOffset =
            WindowInsets.statusBars.getTop(density) + with(density) { ExpandedTopGap.toPx() }

        var offsetY by remember { mutableFloatStateOf(hiddenOffset) }
        // 자동 닫힘 판단용: 지금 손을 대고 있는지 + 마지막으로 만진 시각 (만질 때마다 2초 타이머 리셋)
        var sheetPressed by remember(savedMission?.id) { mutableStateOf(false) }
        var lastTouchAt by remember(savedMission?.id) { mutableLongStateOf(System.currentTimeMillis()) }

        val scope = rememberCoroutineScope()
        var animJob by remember { mutableStateOf<Job?>(null) }

        // 정착점으로 이동. 위로 갈 땐 빠르게, 아래로 갈 땐 2배 느리게.
        fun animateSheetTo(target: Float, onArrived: () -> Unit = {}) {
            animJob?.cancel()
            animJob = scope.launch {
                val spec = if (target > offsetY) DescentSpec else AscentSpec
                animate(offsetY, target, animationSpec = spec) { value, _ -> offsetY = value }
                onArrived()
            }
        }

        // 손가락 이동만큼 시트 이동(범위 제한).
        fun dragBy(delta: Float): Float {
            val new = (offsetY + delta).coerceIn(expandedOffset, hiddenOffset)
            val consumed = new - offsetY
            if (consumed != 0f) offsetY = new
            return consumed
        }

        // 놓았을 때: 빠르게 튕겼으면 그 방향의 다음 정착점, 아니면 가장 가까운 정착점으로.
        fun settle(velocity: Float) {
            val anchors = listOf(expandedOffset, peekOffset, hiddenOffset)
            val target = when {
                velocity < -SETTLE_VELOCITY -> anchors.filter { it < offsetY }.maxOrNull() ?: expandedOffset
                velocity > SETTLE_VELOCITY -> anchors.filter { it > offsetY }.minOrNull() ?: hiddenOffset
                else -> anchors.minByOrNull { abs(it - offsetY) } ?: peekOffset
            }
            animateSheetTo(target) { if (target == hiddenOffset) onDismiss() }
        }

        // 내려가는 애니메이션 동안 보여줄 마지막 내용 (상태가 먼저 비면 시트가 애니메이션 없이 뚝 사라짐)
        var displayedMission by remember { mutableStateOf(savedMission) }
        var displayedRecent by remember { mutableStateOf(recentSavedMissions) }
        if (savedMission != null) {
            displayedMission = savedMission
            displayedRecent = recentSavedMissions
        }

        // 화면 상태 → 시트 올리고 내리기. id 기준이라 내려가는 도중 다른 미션을 저장해도
        // 내려가던 걸 끊고 새 내용으로 다시 올라옴.
        LaunchedEffect(savedMission?.id, savedMission?.isSaved) {
            when {
                savedMission == null -> animateSheetTo(hiddenOffset)
                savedMission.isSaved -> animateSheetTo(peekOffset)
                else -> {
                    // 시트에 뜬 미션이 해제됨: 회색으로 바뀐 아이콘을 잠깐 보여준 뒤 내려감
                    delay(300)
                    animateSheetTo(hiddenOffset) { onDismiss() }
                }
            }
        }
        // 자동 닫힘: 손을 뗀 뒤 2초 동안 안 만지면 스윽 내려감. 만질 때마다 타이머 리셋.
        // 예외: 손을 대고 있는 동안 / 끝까지 펼쳐놓은 상태에선 안 닫힘.
        LaunchedEffect(savedMission?.id) {
            if (savedMission == null) return@LaunchedEffect
            var closing = false
            while (true) {
                delay(100)
                val fullyExpanded = offsetY <= expandedOffset + 1f
                if (sheetPressed || fullyExpanded) {
                    closing = false
                    continue
                }
                val idleFor = System.currentTimeMillis() - lastTouchAt
                if (idleFor >= AUTO_DISMISS_MILLIS) {
                    if (!closing) {
                        closing = true
                        animateSheetTo(hiddenOffset) { onDismiss() }
                    }
                } else {
                    closing = false
                }
            }
        }

        // 시트를 화면에 그릴지 여부 (내려가는 애니 동안에도 그려야 하니 offsetY로 판단).
        val sheetTopForNav = if (savedMission != null || offsetY < hiddenOffset - 0.5f) offsetY else null
        val sheetVisible = sheetTopForNav != null
        // 하단 네비에 시트 위 끝 위치를 매 프레임 알림 → 네비는 시트에 안 덮인 부분만 그림.
        SideEffect { onSheetTopChange(sheetTopForNav) }
        DisposableEffect(Unit) {
            onDispose { onSheetTopChange(null) }
        }

        // 시트 안 카드 스크롤과 시트 끌기 연결: 위로 밀면 시트가 먼저 펼쳐지고, 아래로 당기면 시트가 내려감.
        // source == UserInput 조건: 손가락 스크롤만 시트 드래그로 연결한다. 저장 목록에서 카드가 빠져
        // 내용 높이가 줄면 Compose가 스크롤 위치를 자동 보정(SideEffect)하는데, 이걸 사용자 입력으로
        // 오해하면 시트가 순간 위로 튀었다 내려간다 → UserInput일 때만 반응하게 걸러냄.
        val nestedScrollConnection = object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset =
                if (source == NestedScrollSource.UserInput && available.y < 0) Offset(0f, dragBy(available.y)) else Offset.Zero

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset =
                if (source == NestedScrollSource.UserInput && available.y > 0) Offset(0f, dragBy(available.y)) else Offset.Zero

            override suspend fun onPreFling(available: Velocity): Velocity {
                val atAnchor = offsetY == expandedOffset || offsetY == peekOffset || offsetY == hiddenOffset
                return if (!atAnchor) {
                    settle(available.y)
                    available
                } else {
                    Velocity.Zero
                }
            }
        }

        if (sheetVisible) {
            displayedMission?.let { mission ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        // 시트 높이 = 펼쳤을 때 보이는 만큼. 살짝 상태에선 아래가 화면 밖으로 잘림(디자인 동일).
                        .height(with(density) { (hiddenOffset - expandedOffset).toDp() })
                        .offset { IntOffset(0, offsetY.roundToInt()) }
                        .nestedScroll(nestedScrollConnection)
                        .shadow(elevation = 8.dp, shape = SheetShape) // CSS 0 -8 24 6% 근사
                        .clip(SheetShape)
                        .background(Gray100) // 시트 배경 (CSS Frame 455)
                        // 손이 닿아 있는 동안 + 뗀 순간부터 2초를 재기 위한 감지 (이벤트는 소비 안 함)
                        .pointerInput(savedMission?.id) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    sheetPressed = event.changes.any { it.pressed }
                                    lastTouchAt = System.currentTimeMillis()
                                }
                            }
                        }
                        .draggable(
                            orientation = Orientation.Vertical,
                            state = rememberDraggableState { delta -> dragBy(delta) },
                            onDragStarted = { animJob?.cancel() },
                            onDragStopped = { velocity -> settle(velocity) },
                        ),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        SheetDragHandle()
                        MissionSaveSheetContent(
                            savedMission = mission,
                            recentSavedMissions = displayedRecent,
                            onMissionClick = onMissionClick,
                            onToggleSave = onToggleSave,
                            onSavedListClick = onSavedListClick,
                        )
                    }
                }
            }
        }
    }
}

// 드래그 핸들 (CSS Frame 453): 36x4 Gray600 알약. 위 20 = 시트 위 패딩(CSS), 아래 12 = 내용과 간격(gap).
@Composable
private fun SheetDragHandle() {
    Box(
        modifier = Modifier
            .padding(top = 20.dp, bottom = 12.dp)
            .width(36.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Gray600),
    )
}

@Composable
private fun MissionSaveSheetContent(
    savedMission: MissionListItem,
    recentSavedMissions: List<MissionListItem>,
    onMissionClick: (Long) -> Unit,
    onToggleSave: (Long) -> Unit,
    onSavedListClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()) // 내용이 시트보다 길어지면(저장 많음) 스크롤
            .padding(start = 16.dp, end = 16.dp, bottom = 20.dp), // 시트 안쪽 여백 (CSS padding 20 16)
        verticalArrangement = Arrangement.spacedBy(12.dp), // 묶음 간격 (CSS Frame 457 gap)
    ) {
        // "저장됨" + 방금 저장한 카드 (CSS Frame 456, gap 8)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "저장됨", style = TqType.BodyL.figma(), color = Gray900)
            MissionCard(
                mission = savedMission,
                onClick = { onMissionClick(savedMission.id) },
                onToggleSave = { onToggleSave(savedMission.id) },
            )
        }
        // "저장 목록 >" + 최근 저장 카드 (CSS Frame 451, gap 8) — 다른 저장 미션이 없으면 통째로 숨김
        if (recentSavedMissions.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onSavedListClick),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "저장 목록", style = TqType.BodyL.figma(), color = Gray700)
                    // 오른쪽 화살표 (CSS chevoren_left 좌우반전, Gray/600), 44 터치영역
                    Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "저장 목록 열기",
                            tint = Gray600,
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { // 카드 간격 (CSS Frame 450 gap)
                    recentSavedMissions.forEach { mission ->
                        MissionCard(
                            mission = mission,
                            onClick = { onMissionClick(mission.id) },
                            onToggleSave = { onToggleSave(mission.id) },
                        )
                    }
                }
            }
        }
    }
}

// ── Preview (시트 내용만 확인 — 올라오는 동작은 에뮬에서) ──
@Preview(name = "저장 시트 내용", showBackground = true, backgroundColor = 0xFFF1F5F9)
@Composable
private fun MissionSaveSheetContentPreview() {
    TalkQQuestTheme {
        MissionSaveSheetContent(
            savedMission = MissionListItem(1, "처음 보는 사람에게 짧게 인사하기", "짧은 대화", "쉬움", 2, 20, isSaved = true),
            recentSavedMissions = listOf(
                MissionListItem(2, "최근 본 영화 이야기하기", "짧은 대화", "쉬움", 5, 20, isSaved = true),
                MissionListItem(3, "학교 생활 꿀팁 나누기", "일상 대화", "보통", 8, 30, isSaved = true),
            ),
            onMissionClick = {}, onToggleSave = {}, onSavedListClick = {},
        )
    }
}

// 이 미션이 첫 저장이라 다른 저장 미션이 없는 경우 — "저장 목록" 부분이 숨겨지는지 확인.
@Preview(name = "저장 시트 - 첫 저장", showBackground = true, backgroundColor = 0xFFF1F5F9)
@Composable
private fun MissionSaveSheetFirstSavePreview() {
    TalkQQuestTheme {
        MissionSaveSheetContent(
            savedMission = MissionListItem(1, "처음 보는 사람에게 짧게 인사하기", "짧은 대화", "쉬움", 2, 20, isSaved = true),
            recentSavedMissions = emptyList(),
            onMissionClick = {}, onToggleSave = {}, onSavedListClick = {},
        )
    }
}
