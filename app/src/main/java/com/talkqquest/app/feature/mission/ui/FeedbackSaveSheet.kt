package com.talkqquest.app.feature.mission.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.component.TqSaveSheetScaffold
import com.talkqquest.app.feature.mission.data.model.SavedPhraseItem

// ── 문장 저장 시트 (UI 5차 "문장 저장시 바텀 시트" 프레임 전사) ──
// 피드백 상세(4개 항목 전부)에서 "베스트 문장" 옆 북마크를 누르면 올라옴.
// 5차 CSS를 기존 피드백 상세 프레임과 대조하면 화면 본체는 한 줄도 안 바뀌고 시트만 추가됨.
// 시트 컨테이너(Frame 455)는 미션·리포트 저장 시트와 완전히 같아 core의 TqSaveSheetScaffold
// 재사용 — 카드만 문장 카드(Frame 427321182/183).

// 화면 내용을 감싸서 저장 시트를 위에 겹쳐 그리는 틀.
// savedPhrase가 생기면 시트가 올라오고, null이 되면 내려감.
// ── C담당(아카이브) 연결 지점 ──
// onArchiveClick : 시트의 "보관함 >" — 아카이브 보관함(문장 탭)으로 보내면 됨
// onPhraseClick  : 저장된 문장 카드 클릭 — 보관함 문장 상세("베스트 문장")로 보내면 됨
// 둘 다 NavGraph의 `composable(FEEDBACK_DETAIL)` 한 곳에서 navigate만 채우면 연결 끝.
@Composable
internal fun FeedbackSaveSheetScaffold(
    savedPhrase: SavedPhraseItem?,
    recentSavedPhrases: List<SavedPhraseItem>,
    onDismiss: () -> Unit,
    onToggleSave: (Long) -> Unit,
    onSheetTopChange: (Float?) -> Unit = {}, // 시트 위 끝 y(px), null=시트 없음 — 하단 네비 가림 처리
    onArchiveClick: () -> Unit = {},
    onPhraseClick: (Long) -> Unit = {},
    content: @Composable () -> Unit,
) {
    // 내려가는 애니메이션 동안 보여줄 마지막 목록 (항목 유지는 스캐폴드가 하고, 목록은 여기서)
    var displayedRecent by remember { mutableStateOf(recentSavedPhrases) }
    if (savedPhrase != null) displayedRecent = recentSavedPhrases

    TqSaveSheetScaffold(
        savedItem = savedPhrase,
        itemKey = savedPhrase?.id,
        itemIsSaved = savedPhrase?.isSaved == true,
        onDismiss = onDismiss,
        onSheetTopChange = onSheetTopChange,
        sheetContent = { phrase ->
            FeedbackSaveSheetContent(
                savedPhrase = phrase,
                recentSavedPhrases = displayedRecent,
                onToggleSave = onToggleSave,
                onArchiveClick = onArchiveClick,
                onPhraseClick = onPhraseClick,
            )
        },
        content = content,
    )
}

@Composable
private fun FeedbackSaveSheetContent(
    savedPhrase: SavedPhraseItem,
    recentSavedPhrases: List<SavedPhraseItem>,
    onToggleSave: (Long) -> Unit,
    onArchiveClick: () -> Unit = {},
    onPhraseClick: (Long) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()) // 보관함 문장이 많으면 시트 안에서 스크롤
            // 시트 안쪽 여백 (CSS padding 20 16). 보관함 카드가 있을 땐 마지막 카드 뒤
            // Spacer 12(카드 간격 몫)가 있어 8로 채움(12+8=20) — 미션·리포트 시트와 같은 셈.
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = if (recentSavedPhrases.isEmpty()) 20.dp else 8.dp,
            ),
    ) {
        // "저장됨" + 방금 저장한 문장 카드 (CSS Frame 456, gap 8)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "저장됨", style = TqType.BodyL.figma(), color = Gray900)
            SavedPhraseCard(item = savedPhrase, onToggleSave = onToggleSave, onClick = onPhraseClick)
        }
        // "보관함 >" + 저장된 문장 카드들 (CSS Frame 451) — 카드가 다 빠지면 제목까지 접혀 사라짐
        AnimatedVisibility(
            visible = recentSavedPhrases.any { it.isSaved },
            enter = fadeIn(tween(300)) + expandVertically(tween(300)),
            exit = fadeOut(tween(350, delayMillis = 250)) + shrinkVertically(tween(350, delayMillis = 250)),
        ) {
            Column {
                Spacer(Modifier.height(12.dp)) // 묶음 간격 (CSS Frame 457 gap — 섹션과 함께 접히게 안쪽에)
                Row(
                    modifier = Modifier
                        // CSS: "보관함" 텍스트에 margin 0 -6px (미션·리포트 시트와 동일 Frame 447)
                        .offset(x = (-6).dp)
                        .clip(RoundedCornerShape(12.dp))
                        // C담당 연결 지점: 아카이브 보관함(문장 탭)으로 (NavGraph에서 주입)
                        .clickable(onClick = onArchiveClick),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "보관함", style = TqType.BodyL.figma(), color = Gray700)
                    Box(
                        modifier = Modifier
                            .offset(x = (-6).dp) // margin 0 -6px의 오른쪽 몫
                            .size(44.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_mission_shortcut),
                            contentDescription = "보관함 열기",
                            tint = Color.Unspecified, // 벡터에 색 포함(Gray500)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp)) // 제목 ↔ 카드 (CSS gap 8)
                recentSavedPhrases.forEach { item ->
                    key(item.id) {
                        // 해제: 보라 풀림을 250ms 보여준 뒤 카드가 아래로 가라앉으며 접혀 사라짐
                        // (미션·리포트 시트와 동일 연출). 연출 중 다시 누르면 복구.
                        AnimatedVisibility(
                            visible = item.isSaved,
                            enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                            exit = shrinkVertically(tween(350, delayMillis = 250)) +
                                slideOutVertically(tween(350, delayMillis = 250)) { it / 2 } +
                                fadeOut(tween(350, delayMillis = 250)),
                        ) {
                            Column {
                                SavedPhraseCard(item = item, onToggleSave = onToggleSave, onClick = onPhraseClick)
                                Spacer(Modifier.height(12.dp)) // 카드 간격 (CSS Frame 450 gap)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 저장된 문장 카드 (CSS Frame 427321182: 361x72, 흰 배경 r20 — 이 카드도 CSS에 그림자 없음.
// 미션 카드(그림자 1%)와 다른 게 CSS 그대로임 — 리포트 카드와 동일)
@Composable
private fun SavedPhraseCard(
    item: SavedPhraseItem,
    onToggleSave: (Long) -> Unit,
    onClick: (Long) -> Unit = {}, // C담당 연결 지점: 보관함 문장 상세로
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = { onClick(item.id) })
            .background(Color.White)
            .padding(start = 16.dp, end = 6.dp, top = 12.dp, bottom = 12.dp), // CSS padding 12 6 12 16
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 노트 그림: 48 컨테이너 안에 40 이미지 중앙 (CSS Frame 427321187 48x48 / 이미지 40x40 left4 top4)
        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(R.drawable.img_feedback_note),
                contentDescription = null, // 장식 — 카드의 문장이 이미 내용을 설명함
                modifier = Modifier.size(40.dp),
            )
        }
        Spacer(Modifier.width(12.dp)) // 그림 ↔ 텍스트 (CSS Frame 427321179 gap 12)
        Column(modifier = Modifier.weight(1f)) {
            // 저장한 문장 (CSS 목업처럼 큰따옴표 + 1줄 말줄임 — 폭 214에 …로 잘려 있음)
            Text(
                text = "“${item.phrase}”",
                style = TqType.BodyL.figma().copy(fontWeight = FontWeight.Medium), // Body/L Medium (CSS)
                color = Gray900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // 메타줄: "문장 저장"(고정 문구) | 저장 날짜 (CSS Frame 347 — 구분선 양옆 10)
            Row(
                modifier = Modifier.padding(horizontal = 2.dp), // CSS Frame 347 padding 0 2
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "문장 저장", style = TqType.Caption.figma(), color = Gray500)
                Spacer(Modifier.width(10.dp))
                Box(Modifier.width(1.dp).height(9.dp).background(Gray300)) // 구분선 (CSS Frame 346)
                Spacer(Modifier.width(10.dp))
                Text(text = item.savedDate, style = TqType.Caption.figma(), color = Gray500)
            }
        }
        // 북마크 (CSS 기본 북마크 44x44 / favourite 24) — 아이콘은 미션 카드와 같은 리소스.
        // 리플(물결) 끔 — 아이콘 색 변화만으로 반응 표시 (미션 카드와 같은 규칙, 사용자 결정)
        Box(
            modifier = Modifier
                .size(44.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onToggleSave(item.id) },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(
                    if (item.isSaved) R.drawable.ic_mission_bookmark_filled else R.drawable.ic_mission_bookmark,
                ),
                contentDescription = if (item.isSaved) "북마크 해제" else "북마크",
            )
        }
    }
}

// ── Preview (시트 내용만 확인 — 올라오는 동작은 에뮬에서) ──
private val previewPhrases = listOf(
    SavedPhraseItem(1, "그렇군요! 저도 편해서 놀랐어요", "2026.08.20"),
    SavedPhraseItem(2, "그 말씀 들으니 저도 기분이 좋아지네요", "2026.08.19"),
    SavedPhraseItem(3, "혹시 그때 어떤 기분이셨어요?", "2026.08.18"),
    SavedPhraseItem(4, "좋은 이야기 들려주셔서 감사해요", "2026.08.17"),
    SavedPhraseItem(5, "저도 비슷한 경험이 있어서 공감돼요", "2026.08.16"),
)

@Preview(name = "문장 저장 시트 내용", showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun FeedbackSaveSheetContentPreview() {
    TalkQQuestTheme {
        FeedbackSaveSheetContent(
            savedPhrase = SavedPhraseItem(100, "그렇군요! 저도 편해서 놀랐어요", "2026.08.20"),
            recentSavedPhrases = previewPhrases,
            onToggleSave = {},
        )
    }
}

// 첫 저장이라 보관함이 빈 경우 — "보관함" 부분이 숨겨지는지 확인.
@Preview(name = "문장 저장 시트 - 첫 저장", showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun FeedbackSaveSheetFirstSavePreview() {
    TalkQQuestTheme {
        FeedbackSaveSheetContent(
            savedPhrase = SavedPhraseItem(100, "그렇군요! 저도 편해서 놀랐어요", "2026.08.20"),
            recentSavedPhrases = emptyList(),
            onToggleSave = {},
        )
    }
}
