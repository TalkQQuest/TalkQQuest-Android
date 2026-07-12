package com.talkqquest.app.feature.report.ui

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import com.talkqquest.app.feature.report.data.model.SavedReportItem

// ── 리포트 저장 시트 (CSS "리포트 저장"/"주간 비교 리포트"(시트) 프레임 전사) ──
// "리포트 저장하기" 버튼을 누르면 올라옴. 시트 컨테이너(Frame 455)는 미션 저장 시트와
// 완전히 동일해 core의 TqSaveSheetScaffold 재사용 — 카드만 리포트 카드(Frame 427321183).

// 화면 내용을 감싸서 저장 시트를 위에 겹쳐 그리는 틀.
// savedReport가 생기면 시트가 올라오고, null이 되면 내려감.
// ── C담당(아카이브) 연결 지점 ──
// onArchiveClick : 시트의 "보관함 >" — 아카이브 보관함(리포트 탭)으로 보내면 됨
// onReportClick  : 저장된 리포트 카드 클릭 — 보관함 리포트 상세로 보내면 됨
// 둘 다 NavGraph의 `composable(Screen.REPORT)` 한 곳에서 navigate만 채우면 연결 끝.
@Composable
internal fun ReportSaveSheetScaffold(
    savedReport: SavedReportItem?,
    recentSavedReports: List<SavedReportItem>,
    onDismiss: () -> Unit,
    onToggleSave: (Long) -> Unit,
    onSheetTopChange: (Float?) -> Unit = {}, // 시트 위 끝 y(px), null=시트 없음 — 하단 네비 가림 처리
    onArchiveClick: () -> Unit = {},
    onReportClick: (Long) -> Unit = {},
    content: @Composable () -> Unit,
) {
    // 내려가는 애니메이션 동안 보여줄 마지막 목록 (항목 유지는 스캐폴드가 하고, 목록은 여기서)
    var displayedRecent by remember { mutableStateOf(recentSavedReports) }
    if (savedReport != null) displayedRecent = recentSavedReports

    TqSaveSheetScaffold(
        savedItem = savedReport,
        itemKey = savedReport?.id,
        itemIsSaved = savedReport?.isSaved == true,
        onDismiss = onDismiss,
        onSheetTopChange = onSheetTopChange,
        sheetContent = { report ->
            ReportSaveSheetContent(
                savedReport = report,
                recentSavedReports = displayedRecent,
                onToggleSave = onToggleSave,
                onArchiveClick = onArchiveClick,
                onReportClick = onReportClick,
            )
        },
        content = content,
    )
}

@Composable
private fun ReportSaveSheetContent(
    savedReport: SavedReportItem,
    recentSavedReports: List<SavedReportItem>,
    onToggleSave: (Long) -> Unit,
    onArchiveClick: () -> Unit = {},
    onReportClick: (Long) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()) // 내용이 시트보다 길어지면(저장 많음) 스크롤
            // 시트 안쪽 여백 (CSS padding 20 16). 보관함 카드가 있을 땐 마지막 카드 뒤
            // Spacer 12(카드 간격 몫)가 있어 8로 채움(12+8=20) — 미션 시트와 같은 셈.
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = if (recentSavedReports.isEmpty()) 20.dp else 8.dp,
            ),
    ) {
        // "저장됨" + 방금 저장한 리포트 카드 (CSS Frame 456, gap 8)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "저장됨", style = TqType.BodyL.figma(), color = Gray900)
            SavedReportCard(report = savedReport, onToggleSave = onToggleSave, onClick = onReportClick)
        }
        // "보관함 >" + 저장된 리포트 카드 (CSS Frame 451) — 카드가 다 빠지면 제목까지 접혀 사라짐
        AnimatedVisibility(
            visible = recentSavedReports.any { it.isSaved },
            enter = fadeIn(tween(300)) + expandVertically(tween(300)),
            exit = fadeOut(tween(350, delayMillis = 250)) + shrinkVertically(tween(350, delayMillis = 250)),
        ) {
            Column {
                Spacer(Modifier.height(12.dp)) // 묶음 간격 (CSS Frame 457 gap — 섹션과 함께 접히게 안쪽에)
                Row(
                    modifier = Modifier
                        // CSS: "보관함" 텍스트에 margin 0 -6px (미션 시트와 동일 Frame 447)
                        .offset(x = (-6).dp)
                        .clip(RoundedCornerShape(12.dp))
                        // C담당 연결 지점: 아카이브 보관함(리포트 탭)으로 (NavGraph에서 주입)
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
                recentSavedReports.forEach { report ->
                    key(report.id) {
                        // 해제: 보라 풀림을 250ms 보여준 뒤 카드가 아래로 가라앉으며 접혀 사라짐
                        // (미션 시트와 동일 연출). 다시 누르면 복구.
                        AnimatedVisibility(
                            visible = report.isSaved,
                            enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                            exit = shrinkVertically(tween(350, delayMillis = 250)) +
                                slideOutVertically(tween(350, delayMillis = 250)) { it / 2 } +
                                fadeOut(tween(350, delayMillis = 250)),
                        ) {
                            Column {
                                SavedReportCard(report = report, onToggleSave = onToggleSave, onClick = onReportClick)
                                Spacer(Modifier.height(12.dp)) // 카드 간격 (CSS Frame 450 gap)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 저장된 리포트 카드 (CSS Frame 427321183: 361x72, 흰 배경 r20 — 이 카드는 CSS에 그림자 없음.
// 미션 카드(그림자 1%)와 다른 게 CSS 그대로임 — 디자이너 확인거리)
@Composable
private fun SavedReportCard(
    report: SavedReportItem,
    onToggleSave: (Long) -> Unit,
    onClick: (Long) -> Unit = {}, // C담당 연결 지점: 보관함 리포트 상세로
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = { onClick(report.id) })
            .background(Color.White)
            .padding(start = 16.dp, end = 6.dp, top = 12.dp, bottom = 12.dp), // CSS padding 12 6 12 16
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 차트 그림: 48 컨테이너 안에 40 이미지 중앙 (CSS Frame 427321187 48x48 / 이미지 40x40 left4 top4)
        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(R.drawable.img_report_chart_small),
                contentDescription = null, // 장식 — 카드 제목이 이미 리포트를 설명함
                modifier = Modifier.size(40.dp),
            )
        }
        Spacer(Modifier.width(12.dp)) // 그림 ↔ 텍스트 (CSS Frame 427321179 gap 12)
        Column(modifier = Modifier.weight(1f)) {
            // 제목 = 미션명 (서버 가변 — CSS 폭 210 고정이라 1줄 말줄임, 사용자 결정)
            Text(
                text = report.title,
                style = TqType.BodyL.figma().copy(fontWeight = FontWeight.Medium), // Body/L Medium (CSS)
                color = Gray900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // 메타줄: "리포트 열람(고정 문구) | 저장 날짜" (CSS Frame 347 — 구분선 양옆 10)
            Row(
                modifier = Modifier.padding(horizontal = 2.dp), // CSS Frame 347 padding 0 2
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "리포트 열람", style = TqType.Caption.figma(), color = Gray500)
                Spacer(Modifier.width(10.dp))
                Box(Modifier.width(1.dp).height(9.dp).background(Gray300)) // 구분선 (CSS Frame 346)
                Spacer(Modifier.width(10.dp))
                Text(text = report.savedDate, style = TqType.Caption.figma(), color = Gray500)
            }
        }
        // 북마크 (CSS 기본 북마크 44x44 / favourite 24) — 아이콘은 미션 카드와 같은 리소스
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape) // 눌림 효과 원형 (아이콘 버튼 관례)
                .clickable(onClick = { onToggleSave(report.id) }),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(
                    if (report.isSaved) R.drawable.ic_mission_bookmark_filled else R.drawable.ic_mission_bookmark,
                ),
                contentDescription = if (report.isSaved) "북마크 해제" else "북마크",
            )
        }
    }
}

// 시트 텍스트는 이걸로 감싸 피그마 line-height 여백을 살림 (홈/미션/리포트와 동일한 로컬 관례)
private val FullLeading = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private fun TextStyle.figma(): TextStyle = copy(lineHeightStyle = FullLeading)

// ── Preview (시트 내용만 확인 — 올라오는 동작은 에뮬에서) ──
@Preview(name = "리포트 저장 시트 내용", showBackground = true, backgroundColor = 0xFFF1F5F9)
@Composable
private fun ReportSaveSheetContentPreview() {
    TalkQQuestTheme {
        ReportSaveSheetContent(
            // 카드 제목 = 이 리포트가 나온 미션명 (CSS 목업과 동일)
            savedReport = SavedReportItem(100, "처음 보는 사람에게 짧게 인사하기", "2026.08.21"),
            recentSavedReports = listOf(
                SavedReportItem(1, "최근 본 영화 이야기 하기", "2026.08.20"),
                SavedReportItem(2, "학교 생활 꿀팁 나누기", "2026.08.19"),
                SavedReportItem(3, "주말 계획 이야기하기", "2026.08.18"),
                SavedReportItem(4, "나의 취미를 소개해보기", "2026.08.17"),
            ),
            onToggleSave = {},
        )
    }
}

// 첫 저장이라 보관함이 빈 경우 — "보관함" 부분이 숨겨지는지 확인.
@Preview(name = "리포트 저장 시트 - 첫 저장", showBackground = true, backgroundColor = 0xFFF1F5F9)
@Composable
private fun ReportSaveSheetFirstSavePreview() {
    TalkQQuestTheme {
        ReportSaveSheetContent(
            savedReport = SavedReportItem(100, "처음 보는 사람에게 짧게 인사하기", "2026.08.21"),
            recentSavedReports = emptyList(),
            onToggleSave = {},
        )
    }
}
