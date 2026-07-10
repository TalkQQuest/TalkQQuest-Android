package com.talkqquest.app.feature.mission.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.talkqquest.app.core.designsystem.Error
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.component.TqButton
import com.talkqquest.app.core.designsystem.component.TqButtonSize
import com.talkqquest.app.feature.mission.data.model.MissionListItem
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.feature.mission.viewmodel.SavedMissionsUiState
import com.talkqquest.app.feature.mission.viewmodel.SavedMissionsViewModel

// ── 저장 목록 (피그마 "북마크→저장목록" 개정판 전사 — 헤더 제목 화면 정중앙) ──
// 저장 시트의 "저장 목록 >"에서 들어옴. 작은 화면은 FitDesign 통째 축소(사용자 결정) + 스크롤.
// 카드 좌우는 CSS가 15인데 미션 목록(16)과 1px 비일관이라 16으로 통일(디자이너 확인거리).
// 하단 네비 없음(CSS에 알약 없음) — bottomBarRoutes 미등록.

private val StatusFilters = listOf("완료", "진행중", "미완료") // 디자인 고정 3종, 기본 = 완료

@Composable
fun SavedMissionsScreen(
    viewModel: SavedMissionsViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onMissionClick: (Long) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SavedMissionsScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::loadSavedMissions,
        onStatusSelect = viewModel::selectStatus,
        onToggleSave = viewModel::toggleSave,
        onMissionClick = onMissionClick,
    )
}

@Composable
private fun SavedMissionsScreen(
    uiState: SavedMissionsUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onStatusSelect: (String) -> Unit = {},
    onToggleSave: (Long) -> Unit = {},
    onMissionClick: (Long) -> Unit = {},
) = FitDesign { // 작은 화면에선 디자인(393x852) 통째 축소 — 다른 화면들과 동일
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

            else -> SavedMissionsContent(
                uiState = uiState,
                onBack = onBack,
                onStatusSelect = onStatusSelect,
                onToggleSave = onToggleSave,
                onMissionClick = onMissionClick,
            )
        }
    }
}

@Composable
private fun SavedMissionsContent(
    uiState: SavedMissionsUiState,
    onBack: () -> Unit,
    onStatusSelect: (String) -> Unit,
    onToggleSave: (Long) -> Unit,
    onMissionClick: (Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        Spacer(Modifier.height(8.dp)) // 상태바(40) → 헤더(top 48) (CSS Frame 427321193)
        // 헤더: 뒤로가기 44 왼끝, 제목 화면 가로 정중앙 (개정판 — 미션 상세 헤더와 같은 패턴).
        // 화살표는 이 화면 CSS가 Gray800 (다른 화면 Gray500과 비일관 — 디자이너 확인거리).
        Box(modifier = Modifier.fillMaxWidth().height(44.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape) // 눌림 효과 원형 (아이콘 버튼 관례)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "뒤로가기",
                    tint = Gray800,
                )
            }
            Text(
                text = "내가 저장한 미션",
                style = TqType.BodyL.figma(),
                color = Gray800,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        Spacer(Modifier.height(22.dp)) // 헤더 끝(92) → 칩(top 114) = 22 (CSS)
        // 진행 상태 필터 칩 (CSS Frame 341: 간격 8) — 미션 목록과 같은 칩 부품 재사용
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatusFilters.forEach { status ->
                MissionFilterChip(
                    label = status,
                    selected = status == uiState.selectedStatus,
                    onClick = { onStatusSelect(status) },
                )
            }
        }

        Spacer(Modifier.height(22.dp)) // 칩 끝(148) → 카드(top 170) = 22 (CSS)
        if (uiState.filteredMissions.isEmpty()) {
            // 빈 상태 디자인 없음 — 미션 목록과 같은 스타일 문구 (디자이너 확인거리)
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "저장한 미션이 없어요", style = TqType.BodyM.figma(), color = Gray500)
            }
        } else {
            // 카드 간격 12(CSS Frame 458 gap)는 spacedBy가 아니라 각 항목 안의 Spacer로 —
            // 해제 퇴장 때 카드가 간격까지 데리고 접혀야 남은 카드들이 자연스럽게 붙음.
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 4.dp), // 4 + 항목 안 12 = 16
            ) {
                items(count = uiState.filteredMissions.size, key = { uiState.filteredMissions[it].id }) { index ->
                    val mission = uiState.filteredMissions[index]
                    // 북마크 해제 퇴장: 보라 풀림을 250ms 보여준 뒤 카드가 아래로 가라앉으며 접혀 사라짐.
                    // 실제 목록 제거는 ViewModel이 연출 끝난 뒤(700ms) 수행. 연출 중 재저장 → 다시 펴짐.
                    AnimatedVisibility(
                        visible = mission.isSaved,
                        enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                        exit = shrinkVertically(tween(350, delayMillis = 250)) +
                            slideOutVertically(tween(350, delayMillis = 250)) { it / 2 } +
                            fadeOut(tween(350, delayMillis = 250)),
                    ) {
                        Column {
                            MissionCard(
                                mission = mission,
                                onClick = { onMissionClick(mission.id) },
                                onToggleSave = { onToggleSave(mission.id) },
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

// ── Preview ──
private val previewSaved = listOf(
    MissionListItem(1, "처음 보는 사람에게 짧게 인사하기", "짧은 대화", "쉬움", 2, 20, isSaved = true, status = "완료"),
    MissionListItem(2, "최근 본 영화 이야기하기", "짧은 대화", "쉬움", 5, 20, isSaved = true, status = "완료"),
    MissionListItem(3, "학교 생활 꿀팁 나누기", "일상 대화", "보통", 8, 30, isSaved = true, status = "완료"),
)

@Preview(name = "저장 목록 (393dp)", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun SavedMissionsScreenPreview() {
    TalkQQuestTheme {
        SavedMissionsScreen(
            uiState = SavedMissionsUiState(savedMissions = previewSaved),
            onBack = {}, onRetry = {},
        )
    }
}

@Preview(name = "저장 목록 - 빈 상태", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun SavedMissionsEmptyPreview() {
    TalkQQuestTheme {
        SavedMissionsScreen(
            uiState = SavedMissionsUiState(savedMissions = emptyList()),
            onBack = {}, onRetry = {},
        )
    }
}
