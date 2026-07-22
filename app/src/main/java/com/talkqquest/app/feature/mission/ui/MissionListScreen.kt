package com.talkqquest.app.feature.mission.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import com.talkqquest.app.R
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.talkqquest.app.core.designsystem.Error
import com.talkqquest.app.core.designsystem.FitDesign
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
import com.talkqquest.app.feature.mission.data.model.MissionListItem
import com.talkqquest.app.feature.mission.viewmodel.MissionListUiState
import com.talkqquest.app.feature.mission.viewmodel.MissionListViewModel
import com.talkqquest.app.feature.mission.viewmodel.missionFilters

// ── 미션 목록 (UI 1차 v2.css 전사) ──
// 화면 = 2단 분리(state hoisting): (1) viewModel 연결용 / (2) 값만 받아 그리는 부분(Preview용). 홈 패턴 동일.
// 미션 카드·난이도 알약·로컬 도구는 MissionCard.kt로 분리(저장 시트와 공용).

@Composable
fun MissionListScreen(
    viewModel: MissionListViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onMissionClick: (String) -> Unit = {},
    onSheetTopChange: (Float?) -> Unit = {}, // 저장 시트 위 끝 y(px), null=없음 — 하단 네비 가림 처리(MainScreen 연결)
    onSavedListClick: () -> Unit = {}, // 시트 "저장 목록 >" → 저장 목록 화면
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
                        MissionListContent(
                            uiState = uiState,
                            onBack = onBack,
                            onFilterSelect = onFilterSelect,
                            onToggleSave = onToggleSave,
                            onMissionClick = onMissionClick,
                        )
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
    onBack: () -> Unit,
    onFilterSelect: (String) -> Unit,
    onToggleSave: (String) -> Unit,
    onMissionClick: (String) -> Unit,
) {
    // 화면 좌우 스와이프로도 필터 전환 (칩 탭 선택은 그대로 유지). FlowRow가 칩을 missionFilters
    // 순서(왼→오, 위→아래)로 깔므로 인덱스 ±1 = 읽기 순서 이동 — 줄 오른쪽 끝이면 다음 줄 왼쪽으로 순환.
    // 가로 드래그만 감지해 세로 스크롤과 충돌 안 함.
    val currentFilter by rememberUpdatedState(uiState.selectedFilter)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .pointerInput(Unit) {
                var accum = 0f
                val threshold = 48.dp.toPx()
                detectHorizontalDragGestures(
                    onDragStart = { accum = 0f },
                    onHorizontalDrag = { _, delta -> accum += delta },
                    onDragEnd = {
                        val idx = missionFilters.indexOf(currentFilter)
                        if (idx >= 0) {
                            val n = missionFilters.size
                            when {
                                accum <= -threshold -> onFilterSelect(missionFilters[(idx + 1) % n]) // 왼쪽으로 밀기 → 다음 칩
                                accum >= threshold -> onFilterSelect(missionFilters[(idx - 1 + n) % n]) // 오른쪽으로 밀기 → 이전 칩
                            }
                        }
                    },
                )
            },
        verticalArrangement = Arrangement.spacedBy(14.dp), // 카드 간격 14 (CSS Frame 360 gap)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) { // 콘텐츠 좌우 16 (CSS Frame 431 left)
                Spacer(Modifier.height(8.dp)) // 상태바(40) → 뒤로가기(top 48) (CSS chevoren_left)
                // 뒤로가기 (CSS chevoren_left): 44 터치영역이 화면 왼끝(left 0), 아이콘 Gray500.
                Box(
                    modifier = Modifier
                        .offset(x = (-16).dp) // 콘텐츠 좌우 16 패딩 상쇄 → 터치영역 left 0
                        .size(44.dp)
                        .clip(CircleShape) // 눌림 효과를 네모 대신 동그라미로 (아이콘 버튼 관례)
                        .clickable(onClick = onBack),
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
                MissionFilterChips(
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelect = onFilterSelect,
                )
                Spacer(Modifier.height(10.dp)) // 칩 → 목록 24 (CSS Frame 431 gap) = 10 + 카드간격 14
            }
        }

        if (uiState.filteredMissions.isEmpty()) {
            item {
                // 빈 목록 화면이 피그마에 없음 → 임시 문구. TODO(디자인): 빈 상태 디자인 확정 시 교체.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 80.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "해당하는 미션이 없어요", style = TqType.BodyM.figma(), color = Gray500)
                }
            }
        } else {
            items(uiState.filteredMissions, key = { it.id }) { mission ->
                MissionCard(
                    mission = mission,
                    onClick = { onMissionClick(mission.id) },
                    onToggleSave = { onToggleSave(mission.id) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }

        item { Spacer(Modifier.height(100.dp)) } // 목록 끝 여백 — 떠 있는 하단 네비에 안 가리게 (홈과 동일)
    }
}

// 필터 칩 2줄 (CSS Frame 341: 줄 간격 10, 칩 간격 8). 폭 넘치면 자동 줄바꿈(FlowRow) — 디자인과 동일 배치.
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MissionFilterChips(
    selectedFilter: String,
    onFilterSelect: (String) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        missionFilters.forEach { filter ->
            MissionFilterChip(
                label = filter,
                selected = filter == selectedFilter,
                onClick = { onFilterSelect(filter) },
            )
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
) {
    val shape = RoundedCornerShape(20.dp)
    val base = if (selected) {
        Modifier.clip(shape).background(Primary600)
    } else {
        Modifier
            .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
            .clip(shape)
            .background(White)
    }
    Box(
        modifier = base
            .clickable(onClick = onClick)
            .height(34.dp)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            // CSS: 선택 = 14/22 굵기 500, 미선택 = 14/22 굵기 400 (Body/M)
            style = if (selected) TqType.BodyM.copy(fontWeight = FontWeight.Medium).figma() else TqType.BodyM.figma(),
            color = if (selected) Primary50 else Gray900,
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
