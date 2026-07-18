package com.talkqquest.app.feature.mission.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.component.TqSaveSheetScaffold
import com.talkqquest.app.feature.mission.data.model.MissionListItem
import kotlinx.coroutines.delay

// ── 저장(북마크) 바텀시트 (UI 1차 v2.css "미션 목록에서 북마크"/"북마크(끌어올린 상태)" 전사) ──
// 미션 목록·미션 상세 공용. 시트 컨테이너(프레임·끌기·자동 닫힘)는 리포트 저장 시트와 완전히
// 같아서 core의 TqSaveSheetScaffold로 올렸고, 여기엔 미션 내용(카드 목록)만 남음.

// 화면 내용을 감싸서 저장 시트를 위에 겹쳐 그리는 틀.
// savedMission이 생기면 시트가 올라오고, null이 되면 내려감.
@Composable
internal fun MissionSaveSheetScaffold(
    savedMission: MissionListItem?,
    recentSavedMissions: List<MissionListItem>,
    onDismiss: () -> Unit,
    onMissionClick: (String) -> Unit,
    onToggleSave: (String) -> Unit,
    onSheetTopChange: (Float?) -> Unit = {}, // 시트 위 끝 y(px), null=시트 없음. 하단 네비가 이 선 아래를 안 그려 시트 뒤에 있던 것처럼 드러남
    onSavedListClick: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    // 내려가는 애니메이션 동안 보여줄 마지막 목록 (항목 유지는 스캐폴드가 하고, 목록은 여기서)
    var displayedRecent by remember { mutableStateOf(recentSavedMissions) }
    if (savedMission != null) displayedRecent = recentSavedMissions

    TqSaveSheetScaffold(
        savedItem = savedMission,
        itemKey = savedMission?.id,
        itemIsSaved = savedMission?.isSaved == true,
        onDismiss = onDismiss,
        onSheetTopChange = onSheetTopChange,
        sheetContent = { mission ->
            MissionSaveSheetContent(
                savedMission = mission,
                recentSavedMissions = displayedRecent,
                onMissionClick = onMissionClick,
                onToggleSave = onToggleSave,
                onSavedListClick = onSavedListClick,
            )
        },
        content = content,
    )
}

@Composable
private fun MissionSaveSheetContent(
    savedMission: MissionListItem,
    recentSavedMissions: List<MissionListItem>,
    onMissionClick: (String) -> Unit,
    onToggleSave: (String) -> Unit,
    onSavedListClick: () -> Unit,
) {
    // "저장 목록" 카드의 해제 퇴장 연출용 표시 목록.
    // 부모 상태(recentSavedMissions)는 저장된 것만 내려와 해제 즉시 항목이 빠짐 → 그대로 그리면
    // 카드가 뚝 사라짐. 빠진 항목을 isSaved=false로 잠깐 붙들어 보라 풀림(250ms) → 아래로
    // 가라앉으며 접힘(350ms)을 보여준 뒤 실제로 비움. 연출 중 재저장하면 카드 복귀(실수 복구).
    var shownRecent by remember { mutableStateOf(recentSavedMissions) }
    LaunchedEffect(recentSavedMissions) {
        val incoming = recentSavedMissions.associateBy { it.id }
        shownRecent = shownRecent.map { old -> incoming[old.id] ?: old.copy(isSaved = false) } +
            recentSavedMissions.filter { new -> shownRecent.none { it.id == new.id } }
        if (shownRecent.any { !it.isSaved }) {
            delay(700) // 퇴장 연출(250+350)이 끝난 뒤 목록에서 실제 제거
            shownRecent = shownRecent.filter { it.isSaved }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()) // 내용이 시트보다 길어지면(저장 많음) 스크롤
            // 시트 안쪽 여백 (CSS padding 20 16). 저장 목록 카드가 있을 땐 마지막 카드 뒤
            // Spacer 12(카드 간격 몫)가 있어 8로 채움(12+8=20) — 퇴장 연출 셈과 맞추기 위함.
            .padding(start = 16.dp, end = 16.dp, bottom = if (shownRecent.isEmpty()) 20.dp else 8.dp),
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
        // "저장 목록 >" + 최근 저장 카드 (CSS Frame 451) — 카드가 다 빠지면 제목까지 접혀 사라짐
        AnimatedVisibility(
            visible = shownRecent.any { it.isSaved },
            enter = fadeIn(tween(300)) + expandVertically(tween(300)),
            exit = fadeOut(tween(350, delayMillis = 250)) + shrinkVertically(tween(350, delayMillis = 250)),
        ) {
            Column {
                Spacer(Modifier.height(12.dp)) // 묶음 간격 (CSS Frame 457 gap — 섹션과 함께 접히게 안쪽에)
                Row(
                    modifier = Modifier
                        // CSS: "보관함" 텍스트에 margin 0 -6px → 텍스트가 왼쪽으로 6, 아이콘은 12 당겨짐
                        // (행 전체 -6 + 아래 아이콘 자체 -6 = CSS 좌표와 동일: 텍스트 -6~36, 아이콘 30~74)
                        .offset(x = (-6).dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onSavedListClick),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // "저장 목록" → "보관함" (디자인 변경 2026-07: 저장 목록 명칭을 보관함으로 통일)
                    Text(text = "보관함", style = TqType.BodyL.figma(), color = Gray700)
                    // 바로가기 아이콘 (피그마 바로가기.svg 8x14 전사, Gray500) — 44 터치영역.
                    // 아이콘·제목행 전체 어디를 눌러도 저장 목록으로 진입(디자인 명시).
                    Box(
                        modifier = Modifier
                            .offset(x = (-6).dp) // margin 0 -6px의 오른쪽 몫
                            .size(44.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_mission_shortcut),
                            contentDescription = "저장 목록 열기",
                            tint = Color.Unspecified, // 벡터에 색 포함(Gray500)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp)) // 제목 ↔ 카드 (CSS gap 8)
                shownRecent.forEach { mission ->
                    key(mission.id) {
                        // 해제: 보라 풀림을 250ms 보여준 뒤 카드가 아래로 가라앉으며 접혀 사라짐
                        // (저장 목록 화면과 동일 연출). 카드 간격 12도 같이 접힘.
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
                                    // "저장 목록" 섹션 카드에만 과녁 (UI v4). 위 "저장됨" 카드엔 없음 —
                                    // CSS 시트 프레임에 과녁이 2개(=이 섹션 카드 수)만 있음.
                                    showTarget = true,
                                )
                                Spacer(Modifier.height(12.dp)) // 카드 간격 (CSS Frame 450 gap)
                            }
                        }
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
            savedMission = MissionListItem("1", "처음 보는 사람에게 짧게 인사하기", "짧은 대화", "쉬움", 2, 20, isSaved = true),
            recentSavedMissions = listOf(
                MissionListItem("2", "최근 본 영화 이야기하기", "짧은 대화", "쉬움", 5, 20, isSaved = true),
                MissionListItem("3", "학교 생활 꿀팁 나누기", "일상 대화", "보통", 8, 30, isSaved = true),
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
            savedMission = MissionListItem("1", "처음 보는 사람에게 짧게 인사하기", "짧은 대화", "쉬움", 2, 20, isSaved = true),
            recentSavedMissions = emptyList(),
            onMissionClick = {}, onToggleSave = {}, onSavedListClick = {},
        )
    }
}
