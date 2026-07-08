package com.talkqquest.app.feature.mission.ui

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.Error
import com.talkqquest.app.core.designsystem.Gray100
import com.talkqquest.app.core.designsystem.Gray1000
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.Primary50
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.Success
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.component.TqButton
import com.talkqquest.app.core.designsystem.component.TqButtonSize
import com.talkqquest.app.feature.mission.data.model.MissionListItem
import com.talkqquest.app.feature.mission.viewmodel.MissionListUiState
import com.talkqquest.app.feature.mission.viewmodel.MissionListViewModel
import com.talkqquest.app.feature.mission.viewmodel.missionFilters

// ── 미션 목록 (UI 1차 v2.css 전사) ──
// 화면 = 2단 분리(state hoisting): (1) viewModel 연결용 / (2) 값만 받아 그리는 부분(Preview용). 홈 패턴 동일.
// 디자인 외 신규색(난이도 알약) — CSS 원본값 그대로, 디자인시스템에 없어 화면 로컬 정의.
private val OrangeText = Color(0xFFEF8F22) // CSS: ORANGE (보통)
private val EasyBg = Color(0xFFE3F4E0)     // 쉬움 알약 배경
private val NormalBg = Color(0xFFFEF3E6)   // 보통 알약 배경
private val HardBg = Color(0xFFFDE5E5)     // 어려움 알약 배경

@Composable
fun MissionListScreen(
    viewModel: MissionListViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onMissionClick: (Long) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MissionListScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::loadMissions,
        onFilterSelect = viewModel::selectFilter,
        onToggleSave = viewModel::toggleSave,
        onMissionClick = onMissionClick,
    )
}

@Composable
private fun MissionListScreen(
    uiState: MissionListUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onFilterSelect: (String) -> Unit,
    onToggleSave: (Long) -> Unit,
    onMissionClick: (Long) -> Unit = {},
) {
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
                MissionListContent(
                    uiState = uiState,
                    onBack = onBack,
                    onFilterSelect = onFilterSelect,
                    onToggleSave = onToggleSave,
                    onMissionClick = onMissionClick,
                )
                // 스크롤 유도 마스크 (CSS): 목록 하단이 배경색으로 서서히 사라짐 (높이 68, 투명→Gray50)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(68.dp)
                        .background(Brush.verticalGradient(listOf(Gray50.copy(alpha = 0f), Gray50))),
                )
            }
        }
    }
}

@Composable
private fun MissionListContent(
    uiState: MissionListUiState,
    onBack: () -> Unit,
    onFilterSelect: (String) -> Unit,
    onToggleSave: (Long) -> Unit,
    onMissionClick: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
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
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
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
private fun MissionFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    val base = if (selected) {
        Modifier.clip(shape).background(Primary600)
    } else {
        Modifier
            .softShadow(color = Gray1000.copy(alpha = 0.04f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
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

// 미션 카드 (CSS Frame 352): 흰 배경, radius 20, 그림자 0 8 24 rgba(15,23,42,0.04), padding 14/20.
// 높이 85 = 제목 1줄일 때. 제목이 길면 줄바꿈되고 카드가 늘어남(말줄임 없음 — 팀 결정).
// 카드 전체 = 미션 상세로 가는 버튼. 북마크만 예외(자기 클릭 소비).
@Composable
private fun MissionCard(
    mission: MissionListItem,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .softShadow(color = Gray1000.copy(alpha = 0.04f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .background(White)
            .padding(start = 20.dp, end = 9.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(7.dp), // 제목 ↔ 메타줄 (CSS Frame 350 gap)
        ) {
            Text(
                text = mission.title.glueShortWords().keepWordsIntact(),
                // CSS: Body/L Medium (16/24, 굵기 500)
                style = TqType.BodyL.figma().copy(fontWeight = FontWeight.Medium, lineBreak = LineBreak.Heading),
                color = Gray900,
            )
            Row(
                // 좁은 화면에서 메타줄이 안 들어가면 글자를 꺾지 말고 가로 스크롤 (393에선 다 들어가 변화 없음)
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp), // 알약 간격 (CSS Frame 349/348 gap)
            ) {
                DifficultyLabel(difficulty = mission.difficulty)
                CategoryTag(category = mission.category)
                TimeXpRow(minutes = mission.estimatedMinutes, xp = mission.rewardXp)
            }
        }
        // 북마크: 44 터치영역 + 아이콘 (미저장 Gray400 테두리 / 저장 Purple600 채움 — drawable 2종)
        Box(
            modifier = Modifier
                .size(44.dp)
                .clickable(onClick = onToggleSave),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(
                    if (mission.isSaved) R.drawable.ic_mission_bookmark_filled else R.drawable.ic_mission_bookmark,
                ),
                contentDescription = if (mission.isSaved) "북마크 해제" else "북마크",
            )
        }
    }
}

// 난이도 알약 (CSS Frame 345): radius 16, padding 4x12, Label/M. 색은 난이도별(CSS 원본값).
@Composable
private fun DifficultyLabel(difficulty: String) {
    val (textColor, bgColor) = when (difficulty) {
        "쉬움" -> Success to EasyBg
        "보통" -> OrangeText to NormalBg
        "어려움" -> Error to HardBg
        else -> Gray700 to Gray100 // 알 수 없는 값 대비(서버 값 변동 방어)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(text = difficulty, style = TqType.LabelM.figma(), color = textColor)
    }
}

// 카테고리 태그 (CSS Frame 342): radius 16, padding 4x12, Gray100 배경, Caption Gray500.
@Composable
private fun CategoryTag(category: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Gray100)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(text = category, style = TqType.Caption.figma(), color = Gray500)
    }
}

// 시간 · 구분선 · XP (CSS Frame 347: [시계 9 + N분] | 1x9 구분선 | [플러스 + NXP], 안쪽 여백 10)
@Composable
private fun TimeXpRow(minutes: Int, xp: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_mission_time),
                contentDescription = null,
                modifier = Modifier.size(9.dp),
            )
            Text(text = "${minutes}분", style = TqType.Caption.figma(), color = Gray500, softWrap = false)
        }
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(9.dp)
                .background(Gray300),
        )
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_mission_xp),
                contentDescription = null,
                modifier = Modifier.size(7.dp),
            )
            Text(text = "${xp}XP", style = TqType.Caption.figma(), color = Gray500, softWrap = false)
        }
    }
}

// ── 홈과 동일한 로컬 도구 복사본 (공통 승격 시 한꺼번에 정리) ──

// CSS box-shadow 그대로 그리는 소프트 그림자 (Modifier.shadow는 오프셋·색·투명도 못 담음).
private fun Modifier.softShadow(
    color: Color,
    offsetY: Dp,
    blur: Dp,
    cornerRadius: Dp,
): Modifier = this.drawBehind {
    val paint = Paint().apply { this.color = color }
    val blurPx = blur.toPx()
    if (blurPx > 0f) {
        paint.asFrameworkPaint().maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
    }
    drawIntoCanvas { canvas ->
        canvas.drawRoundRect(
            left = 0f,
            top = offsetY.toPx(),
            right = size.width,
            bottom = size.height + offsetY.toPx(),
            radiusX = cornerRadius.toPx(),
            radiusY = cornerRadius.toPx(),
            paint = paint,
        )
    }
}

// Compose가 깎는 line-height 위아래 여백을 피그마처럼 살림.
private val FullLeading = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private fun TextStyle.figma(): TextStyle = copy(lineHeightStyle = FullLeading)

// 어절 안 글자 사이에 WORD JOINER(U+2060)를 끼워 어절 중간 줄바꿈("사/람에게")을 막음 (전 버전 동작).
private fun String.keepWordsIntact(): String =
    replace(Regex("(?<=\\S)(?=\\S)"), "⁠")

// 한 글자 어절("한 번"의 "한")이 줄 끝에 홀로 남지 않게 NBSP로 다음 어절과 묶음.
private fun String.glueShortWords(): String =
    replace(Regex("(?<=(^|\\s)\\S) "), " ")

// ── Preview ──
private val previewMissions = listOf(
    MissionListItem(1, "처음 보는 사람에게 짧게 인사하기", "짧은 대화", "쉬움", 2, 20),
    MissionListItem(2, "최근 본 영화 이야기하기", "짧은 대화", "쉬움", 5, 20, isSaved = true),
    MissionListItem(3, "학교 생활 꿀팁 나누기", "일상 대화", "보통", 8, 30, isSaved = true),
    MissionListItem(4, "나의 취미를 소개해보기", "친구 만들기", "어려움", 10, 40),
    MissionListItem(5, "주말 계획 이야기하기", "짧은 대화", "쉬움", 5, 20),
    MissionListItem(6, "동아리에서 관심사가 비슷한 사람에게 먼저 말 걸어보기", "친구 만들기", "어려움", 15, 60),
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
