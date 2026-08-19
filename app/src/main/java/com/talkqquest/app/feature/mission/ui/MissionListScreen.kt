package com.talkqquest.app.feature.mission.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.animateIntSizeAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import com.talkqquest.app.R
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.roundToInt
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.talkqquest.app.core.designsystem.Error
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.component.ChipContentCrossfade
import com.talkqquest.app.core.designsystem.component.rememberDebouncedChipSelection
import com.talkqquest.app.core.designsystem.component.rememberHapticTick
import com.talkqquest.app.core.designsystem.Gray1000
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.Primary50
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.softShadow
import com.talkqquest.app.core.designsystem.component.TqButton
import com.talkqquest.app.core.designsystem.component.TqButtonSize
import com.talkqquest.app.core.designsystem.component.rememberHapticTick
import com.talkqquest.app.feature.mission.data.model.MissionListItem
import com.talkqquest.app.feature.mission.viewmodel.MissionListUiState
import com.talkqquest.app.feature.mission.viewmodel.MissionListViewModel
import com.talkqquest.app.feature.mission.viewmodel.missionFilters

// ── 미션 목록 (UI 1차 v2.css 전사) ──
// 화면 = 2단 분리(state hoisting): (1) viewModel 연결용 / (2) 값만 받아 그리는 부분(Preview용). 홈 패턴 동일.
// 미션 카드·난이도 알약·로컬 도구는 MissionCard.kt로 분리(저장 시트와 공용).

// MissionListViewModel의 difficultyFilters와 값이 같다(그쪽은 private라 못 가져다 씀).
private val difficultyFilterChips = setOf("쉬움", "보통", "어려움")

// 필터 칩 값 → 미션 목록 (순수 함수). MissionListUiState.filteredMissions와 같은 로직이지만,
// ChipContentCrossfade의 content 람다가 "받은 필터 값"만으로 목록을 그리게 하기 위해 뽑아냈다.
private fun filterMissionsByChip(missions: List<MissionListItem>, filter: String): List<MissionListItem> =
    when {
        filter == "전체" -> missions
        filter in difficultyFilterChips -> missions.filter { it.difficulty == filter }
        else -> missions.filter { it.category == filter }
    }

@Composable
fun MissionListScreen(
    viewModel: MissionListViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onMissionClick: (String) -> Unit = {},
    onSheetTopChange: (Float?) -> Unit = {}, // 저장 시트 위 끝 y(px), null=없음 — 하단 네비 가림 처리(MainScreen 연결)
    onSavedListClick: () -> Unit = {}, // 시트 "저장 목록 >" → 저장 목록 화면
    onArchiveClick: () -> Unit = {}, // 헤더 폴더 → 보관함 (UI 12)
    // homeContext=true: 홈 "다른 미션 보기"로 띄운 화면. 헤더를 예전 CSS(뒤로가기 + "미션 목록")로 표시하고
    // 홈 탭을 유지한다. false(기본)=미션 탭의 새 헤더("OO님을 위한 미션" + 폴더). 본문·북마크는 공유.
    homeContext: Boolean = false,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // 다른 화면(상세·저장 목록)에서 바꾼 북마크가 돌아왔을 때 반영되도록, 복귀마다 조용히 재조회.
    // 동시에 "저장됨" 시트도 닫음 — 시트 뒤 목록 카드로 상세 갔다 오면 시트가 재등장하던 버그 방지.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.loadMissions(showLoading = false)
        viewModel.dismissSaveSheet()
    }
    MissionListScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::loadMissions,
        onFilterSelect = viewModel::selectFilter,
        onToggleSave = viewModel::toggleSave,
        onDismissSaveSheet = viewModel::dismissSaveSheet,
        onMissionClick = onMissionClick,
        onSheetTopChange = onSheetTopChange,
        onSavedListClick = onSavedListClick,
        onArchiveClick = onArchiveClick,
        homeContext = homeContext,
    )
}

@Composable
private fun MissionListScreen(
    uiState: MissionListUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onFilterSelect: (String) -> Unit,
    onToggleSave: (String) -> Unit,
    onDismissSaveSheet: () -> Unit = {},
    onMissionClick: (String) -> Unit = {},
    onSheetTopChange: (Float?) -> Unit = {},
    onSavedListClick: () -> Unit = {},
    homeContext: Boolean = false,
    onArchiveClick: () -> Unit = {},
) = FitDesign { // 작은 화면에선 디자인(393x852) 통째 축소 — 저장 시트 포함 (사용자 결정)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50), // 페이지 배경 Gray/50 (CSS)
        contentAlignment = Alignment.Center,
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator(color = Primary600)

            uiState.errorMessage != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = uiState.errorMessage, style = TqType.BodyM.figma(), color = Error)
                    Spacer(Modifier.height(16.dp))
                    TqButton(text = "다시 시도", onClick = onRetry, size = TqButtonSize.Medium)
                }
            }

            else -> {
                // 북마크로 저장하면 목록 위로 "저장됨" 시트가 올라옴 (CSS "미션 목록에서 북마크").
                // 표준 시트라 배경 안 어두워지고 뒤 목록도 계속 스크롤 가능.
                MissionSaveSheetScaffold(
                    savedMission = uiState.saveSheetMission,
                    recentSavedMissions = uiState.otherSavedMissions,
                    onDismiss = onDismissSaveSheet,
                    // 시트에서 카드를 누르면 시트 닫고 미션 상세로
                    onMissionClick = { id ->
                        onDismissSaveSheet()
                        onMissionClick(id)
                    },
                    onToggleSave = onToggleSave,
                    onSheetTopChange = onSheetTopChange,
                    // 시트에서 "저장 목록 >" → 시트 닫고 저장 목록 화면으로
                    onSavedListClick = {
                        onDismissSaveSheet()
                        onSavedListClick()
                    },
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val listState = rememberLazyListState()
                        // 하단 페이드는 "아래 더 있어요" 힌트이므로, 끝까지 스크롤해 더 내릴 게 없으면
                        // 사라져야 함(안 그러면 마지막 카드가 페이드에 영구히 가림). 툭 꺼지면 어색하니
                        // 0.2초에 걸쳐 alpha를 부드럽게 전환.
                        val showFade by remember { derivedStateOf { listState.canScrollForward } }
                        val fadeAlpha by animateFloatAsState(
                            targetValue = if (showFade) 1f else 0f,
                            animationSpec = tween(200),
                            label = "listBottomFade",
                        )
                        MissionListContent(
                            uiState = uiState,
                            listState = listState,
                            onFilterSelect = onFilterSelect,
                            onToggleSave = onToggleSave,
                            onMissionClick = onMissionClick,
                            onArchiveClick = onArchiveClick,
                            onBack = onBack,
                            homeContext = homeContext,
                        )
                        if (uiState.filteredMissions.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "해당하는 미션이 없어요",
                                    style = TqType.BodyM.figma(),
                                    color = Gray500,
                                )
                            }
                        }
                        // 스크롤 유도 마스크 (CSS "스크롤 유도 마스크"): left 16 · 폭 360 · top 670 · 높이 68
                        // = 하단 네비 알약 위에서 목록이 배경색으로 사라짐 (투명→Gray50).
                        // ★재대조(2026-07-22): 예전엔 화면 맨 밑(852)에 전체폭으로 붙여 알약 뒤에 가려
                        //   사실상 안 보였음 → CSS 좌표 그대로 아래에서 114(=852-738) 띄움.
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(start = 16.dp, end = 17.dp, bottom = 114.dp)
                                .fillMaxWidth()
                                .height(68.dp)
                                .alpha(fadeAlpha)
                                .background(Brush.verticalGradient(listOf(Gray50.copy(alpha = 0f), Gray50))),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MissionListContent(
    uiState: MissionListUiState,
    listState: LazyListState,
    onFilterSelect: (String) -> Unit,
    onToggleSave: (String) -> Unit,
    onMissionClick: (String) -> Unit,
    onArchiveClick: () -> Unit,
    onBack: () -> Unit,
    homeContext: Boolean,
) {
    val tick = rememberHapticTick()

    // 칩 강조는 누르는 즉시, 실제 목록 교체(페이드 스루)는 100ms 디바운스 — 연타해도 마지막 선택만 반영.
    val filterSelection = rememberDebouncedChipSelection(
        current = uiState.selectedFilter,
        onCommit = onFilterSelect,
    )
    // 칩(실제 선택)이 바뀌면 스크롤을 애니메이션 없이 맨 위로 되돌린다.
    LaunchedEffect(uiState.selectedFilter) {
        listState.scrollToItem(0)
    }

    // 화면 좌우 스와이프로도 필터 전환 (칩 탭 선택은 그대로 유지). FlowRow가 칩을 missionFilters
    // 순서(왼→오, 위→아래)로 깔므로 인덱스 ±1 = 읽기 순서 이동 — 줄 오른쪽 끝이면 다음 줄 왼쪽으로 순환.
    // 목록 끝 여백. ⚠️ CSS(Frame 431)는 뷰포트 padding:0 + 스크롤 리스트라 "스크롤 끝 여백"을
    // 정의하지 않음 = CSS 침묵 구간. 그래서 이 값은 전사값이 아니라 판단값이다.
    // 하단 네비(알약 h64, 인셋+76)에 안 겹치게 확보 + "목록의 끝" 느낌으로 여유를 둔 것(사용자 결정).
    // ※ 카드 간격(14)과 일치가 목적 아님. CSS가 이 여백을 명시하면 그 값으로 교체할 것.
    val bottomBarClearance = WindowInsets.navigationBars.asPaddingValues()
        .calculateBottomPadding() + 76.dp + 14.dp
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp), // 카드 간격 14 (CSS Frame 360 gap)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) { // 콘텐츠 좌우 16
                if (homeContext) {
                    // 홈 "다른 미션 보기" 진입 = 예전 CSS 헤더(뒤로가기 + "미션 목록"). 홈 탭 유지.
                    Spacer(Modifier.height(8.dp)) // 상태바(40) → 뒤로가기(top 48) (CSS chevoren_left)
                    Box(
                        modifier = Modifier
                            .offset(x = (-16).dp) // 콘텐츠 좌우 16 패딩 상쇄 → 터치영역 left 0
                            .size(44.dp)
                            .clip(CircleShape) // 눌림 효과 동그라미 (아이콘 버튼 관례)
                            .clickable(onClick = { tick(); onBack() }),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back_chevron),
                            contentDescription = "뒤로가기",
                            tint = Gray500,
                        )
                    }
                    Spacer(Modifier.height(4.dp)) // 뒤로가기(48+44) → 제목(top 96) = 4 (CSS)
                    Text(text = "미션 목록", style = TqType.TitleL.figma(), color = Gray700)
                    Spacer(Modifier.height(12.dp)) // 제목 → 칩 (CSS Frame 355 gap 12)
                } else {
                    // 미션 탭 = UI 12 헤더(Frame 427321646, space-between): 제목(왼) + 보관함 폴더(오). 뒤로가기 없음.
                    Spacer(Modifier.height(18.dp)) // 상태바(40) → 콘텐츠(top 58) = 18 (UI 12 Frame 427321648)
                    Row(
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // 제목 "OO님을 위한 미션" (Title/L 18/600 Gray700). OO = 서버 닉네임(유동).
                        Text(
                            text = "${uiState.nickname}님을 위한 미션",
                            style = TqType.TitleL.figma(),
                            color = Gray700,
                        )
                        // 보관함 폴더 (UI 12 archive 44x44, Gray/500 외곽선). 44 터치영역, 우측 끝(콘텐츠 16 안쪽).
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape) // 눌림 효과 동그라미 (아이콘 버튼 관례)
                                .clickable(onClick = onArchiveClick),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_folder_outline),
                                contentDescription = "보관함",
                                tint = Gray500,
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp)) // 제목 → 칩 (UI 12 Frame 427321647 gap 6)
                }
                MissionFilterChips(
                    selectedFilter = filterSelection.displayed,
                    onFilterSelect = filterSelection.select,
                )
                Spacer(Modifier.height(10.dp)) // 칩 → 목록 24 = 10 + 카드간격 14
            }
        }

        // 칩 전환(5-A): 카드별 등장/퇴장이 아니라 목록 전체를 한 덩어리로 페이드 스루 교체한다.
        // 실제 선택(uiState.selectedFilter)이 바뀔 때만 재생 — 북마크만 바뀌었을 땐 재생되지 않는다.
        item {
            ChipContentCrossfade(targetState = uiState.selectedFilter) { selectedFilter ->
                // 받은 selectedFilter로만 걸러 그린다 — 바깥 uiState.selectedFilter(지금 값)를
                // 읽으면 전환 중 나가는 화면과 들어오는 화면이 같은 목록을 겹쳐 그리게 된다.
                val displayMissions = filterMissionsByChip(uiState.missions, selectedFilter)
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    displayMissions.forEach { mission ->
                        MissionCard(
                            mission = mission,
                            onClick = { onMissionClick(mission.id) },
                            onToggleSave = { onToggleSave(mission.id) },
                            modifier = Modifier.padding(horizontal = 16.dp),
                            animateContentChanges = true,
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(bottomBarClearance)) } // 목록 끝 여백 (네비바 위로 카드 간격만큼)
    }
}

// 필터 칩 2줄 (CSS Frame 341: 줄 간격 10, 칩 간격 8). 폭 넘치면 자동 줄바꿈(FlowRow) — 디자인과 동일 배치.
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MissionFilterChips(
    selectedFilter: String,
    onFilterSelect: (String) -> Unit,
) {
    val tick = rememberHapticTick()
    var bounds by remember { mutableStateOf<Map<String, Pair<IntOffset, IntSize>>>(emptyMap()) }
    var parentOffset by remember { mutableStateOf(IntOffset.Zero) }
    var hasMovedSelection by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val selectedBounds = bounds[selectedFilter]
    val animatedOffset by animateIntOffsetAsState(
        targetValue = selectedBounds?.first ?: IntOffset.Zero,
        animationSpec = if (hasMovedSelection) tween(260, easing = FastOutSlowInEasing) else snap(),
        label = "missionFilterSelectionOffset",
    )
    val animatedSize by animateIntSizeAsState(
        targetValue = selectedBounds?.second ?: IntSize.Zero,
        animationSpec = if (hasMovedSelection) tween(260, easing = FastOutSlowInEasing) else snap(),
        label = "missionFilterSelectionSize",
    )
    val selectionAlpha by animateFloatAsState(
        targetValue = if (selectedBounds != null) 1f else 0f,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "missionFilterSelectionAlpha",
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInWindow()
                parentOffset = IntOffset(position.x.roundToInt(), position.y.roundToInt())
            },
    ) {
        bounds.values.forEach { (offset, size) ->
            Box(
                Modifier
                    .offset { offset }
                    .size(
                        with(density) { size.width.toDp() },
                        with(density) { size.height.toDp() },
                    )
                    .softShadow(
                        color = Gray1000.copy(alpha = 0.01f),
                        offsetY = 8.dp,
                        blur = 24.dp,
                        cornerRadius = 20.dp,
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(White),
            )
        }
        if (animatedSize != IntSize.Zero) {
            Box(
                Modifier
                    .offset { animatedOffset }
                    .size(
                        with(density) { animatedSize.width.toDp() },
                        with(density) { animatedSize.height.toDp() },
                    )
                    .graphicsLayer { alpha = selectionAlpha }
                    .clip(RoundedCornerShape(20.dp))
                    .background(Primary600),
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            missionFilters.forEach { filter ->
                MissionFilterChip(
                    label = filter,
                    selected = filter == selectedFilter,
                    selectionOverlay = true,
                    onClick = {
                        if (filter != selectedFilter) {
                            tick()
                            hasMovedSelection = true
                        }
                        onFilterSelect(filter)
                    },
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        val position = coordinates.positionInWindow()
                        bounds = bounds + (
                            filter to (
                                IntOffset(
                                    position.x.roundToInt() - parentOffset.x,
                                    position.y.roundToInt() - parentOffset.y,
                                ) to coordinates.size
                            )
                        )
                    },
                )
            }
        }
        // 기본 글자는 회색으로 유지하고 보라색 선택판과 실제로 겹치는 부분에만
        // 같은 위치의 흰 글자를 드러내 선택판이 지나가는 속도대로 색이 채워지게 한다.
        FlowRow(
            modifier = Modifier.drawWithContent {
                clipRect(
                    left = animatedOffset.x.toFloat(),
                    top = animatedOffset.y.toFloat(),
                    right = (animatedOffset.x + animatedSize.width).toFloat(),
                    bottom = (animatedOffset.y + animatedSize.height).toFloat(),
                ) {
                    this@drawWithContent.drawContent()
                }
            },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            missionFilters.forEach { filter ->
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = filter,
                        style = TqType.BodyM.copy(fontWeight = FontWeight.Medium).figma(),
                        color = Primary50,
                    )
                }
            }
        }
    }
}

// 칩 (CSS select chip): 높이 34, radius 20, 좌우 18.
// 선택 = Purple600 배경 + Primary50 글자 / 미선택 = 흰 배경 + 카드 그림자 + Gray900 글자.
@Composable
internal fun MissionFilterChip( // 저장 목록 화면에서도 재사용 (완료/진행중/미완료 필터)
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectionOverlay: Boolean = false,
) {
    val shape = RoundedCornerShape(20.dp)
    val base = if (selected) {
        Modifier
            .clip(shape)
            .background(if (selectionOverlay) Color.Transparent else Primary600)
    } else {
        if (selectionOverlay) {
            Modifier.clip(shape).background(Color.Transparent)
        } else {
            Modifier
                .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
                .clip(shape)
                .background(White)
        }
    }
    Box(
        modifier = modifier.then(base)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .height(34.dp)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            // 선택 전환 때 굵기가 바뀌면 hug 너비와 뒤 칩 위치가 흔들리므로
            // 피그마 선택 칩 기준인 14/22 Medium으로 고정하고 색상만 전환한다.
            style = TqType.BodyM.copy(fontWeight = FontWeight.Medium).figma(),
            color = if (selectionOverlay) Gray900 else if (selected) Primary50 else Gray900,
        )
    }
}

// ── Preview ──
private val previewMissions = listOf(
    MissionListItem("1", "처음 보는 사람에게 짧게 인사하기", "짧은 대화", "쉬움", 2, 20),
    MissionListItem("2", "최근 본 영화 이야기하기", "짧은 대화", "쉬움", 5, 20, isSaved = true),
    MissionListItem("3", "학교 생활 꿀팁 나누기", "일상 대화", "보통", 8, 30, isSaved = true),
    MissionListItem("4", "나의 취미를 소개해보기", "친구 만들기", "어려움", 10, 40),
    MissionListItem("5", "주말 계획 이야기하기", "짧은 대화", "쉬움", 5, 20),
    MissionListItem("6", "동아리에서 관심사가 비슷한 사람에게 먼저 말 걸어보기", "친구 만들기", "어려움", 15, 60),
)

@Preview(name = "미션 목록 (393dp 실기기)", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun MissionListScreenPreview() {
    TalkQQuestTheme {
        MissionListScreen(
            uiState = MissionListUiState(missions = previewMissions),
            onBack = {}, onRetry = {}, onFilterSelect = {}, onToggleSave = {},
        )
    }
}

// 좁은 화면(320dp): 칩 줄바꿈·카드 메타줄이 안 넘치는지 확인용.
@Preview(name = "미션 목록 (320dp 좁은 화면)", showSystemUi = true, device = "spec:width=320dp,height=640dp")
@Composable
private fun MissionListScreenNarrowPreview() {
    TalkQQuestTheme {
        MissionListScreen(
            uiState = MissionListUiState(missions = previewMissions),
            onBack = {}, onRetry = {}, onFilterSelect = {}, onToggleSave = {},
        )
    }
}

// 필터 결과 0개(빈 목록) 상태.
@Preview(name = "미션 목록 - 빈 목록", showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun MissionListScreenEmptyPreview() {
    TalkQQuestTheme {
        MissionListScreen(
            uiState = MissionListUiState(missions = emptyList()),
            onBack = {}, onRetry = {}, onFilterSelect = {}, onToggleSave = {},
        )
    }
}

@Preview(name = "미션 목록 - 로딩", showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun MissionListScreenLoadingPreview() {
    TalkQQuestTheme {
        MissionListScreen(
            uiState = MissionListUiState(isLoading = true),
            onBack = {}, onRetry = {}, onFilterSelect = {}, onToggleSave = {},
        )
    }
}

@Preview(name = "미션 목록 - 에러", showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun MissionListScreenErrorPreview() {
    TalkQQuestTheme {
        MissionListScreen(
            uiState = MissionListUiState(errorMessage = "네트워크 연결을 확인해주세요."),
            onBack = {}, onRetry = {}, onFilterSelect = {}, onToggleSave = {},
        )
    }
}
