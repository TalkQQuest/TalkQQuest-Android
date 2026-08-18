package com.talkqquest.app.feature.report.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.Error
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray100
import com.talkqquest.app.core.designsystem.Gray1000
import com.talkqquest.app.core.designsystem.Gray200
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.Primary100
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.Primary700
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.component.TqButton
import com.talkqquest.app.core.designsystem.softShadow
import com.talkqquest.app.feature.report.data.model.CategoryRank
import com.talkqquest.app.feature.report.data.model.MetricChange
import com.talkqquest.app.feature.report.data.model.WeeklyCompareDetail
import com.talkqquest.app.feature.report.viewmodel.WeeklyCompareUiState
import com.talkqquest.app.feature.report.viewmodel.WeeklyCompareViewModel
import kotlin.math.abs

// ── 주간 비교 리포트(홈/알림창에서 진입) ──
// UI 14차 "주간 비교 리포트(홈/알림창에서 진입)" 프레임 전사.
// 세로 배치(y): 상태바 40 / 헤더 44 / 주차이동 44 / 지표 2x2 / 하이라이트 / 주제 칩 / 진행률 / 버튼 728.
//
// CSS와 다른 점(전부 사용자 결정):
//  - 하단 버튼 문구는 CSS에 "다음"으로 나오지만 그건 버튼 컴포넌트의 기본 텍스트 레이어 이름이다
//    (파일 전체에 39번 등장). 인스턴스에서 덮어쓴 실제 문구는 시안대로 "리포트 저장하기".
//  - 증감 0일 때 디자인이 없어 하락 배지 색(Gray)에 화살표를 빼고 숫자만 둔다.
//  - 하이라이트 카드 높이는 CSS 76 고정이지만 서버 문장이 길면 잘려서 최소 높이로 바꿨다
//    (알림 카드와 같은 처리).
//  - 주차 이동 화살표 비활성 디자인이 없어 Gray/300으로 대신한다. 단 주차가 하나뿐일 때는
//    시안대로 Purple/600을 유지한다 — 이동할 목록이 아직 없는 것뿐인데 양쪽이 다 회색이 되면
//    기능이 없는 화면처럼 보이고, 이 줄의 유일한 포인트 색이 사라진다.
//  - CSS 좌우 여백이 비대칭이다(본문 좌 15 / 우 11, 버튼 좌 16 / 우 15) — 눈대중으로 맞추지 않고 그대로 뒀다.

@Composable
fun WeeklyCompareScreen(
    onClose: () -> Unit = {},
    // 진행률 카드의 "완료한 미션 >" — 보관함 미션 목록(C 담당)으로 가는 자리.
    onCompletedMissionsClick: () -> Unit = {},
    // 저장 시트의 "보관함 >" / 저장된 리포트 카드 — 보관함으로 가는 자리(NavGraph에서 주입).
    onArchiveClick: () -> Unit = {},
    onReportClick: (reportId: String, isWeeklyCompare: Boolean) -> Unit = { _, _ -> },
    viewModel: WeeklyCompareViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WeeklyCompareScreen(
        uiState = uiState,
        onClose = onClose,
        onPrevWeek = viewModel::showPrevWeek,
        onNextWeek = viewModel::showNextWeek,
        onRetry = viewModel::load,
        onSaveClick = viewModel::saveReport,
        onToggleSaveInSheet = viewModel::toggleReportSave,
        onDismissSaveSheet = viewModel::dismissSaveSheet,
        onCompletedMissionsClick = onCompletedMissionsClick,
        onArchiveClick = onArchiveClick,
        onReportClick = onReportClick,
    )
}

@Composable
fun WeeklyCompareScreen(
    uiState: WeeklyCompareUiState,
    onClose: () -> Unit = {},
    onPrevWeek: () -> Unit = {},
    onNextWeek: () -> Unit = {},
    onRetry: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onToggleSaveInSheet: (String) -> Unit = {},
    onDismissSaveSheet: () -> Unit = {},
    onCompletedMissionsClick: () -> Unit = {},
    onArchiveClick: () -> Unit = {},
    onReportClick: (reportId: String, isWeeklyCompare: Boolean) -> Unit = { _, _ -> },
) = FitDesign { // 작은 화면에선 디자인(393x852) 통째 축소 — 다른 화면들과 동일
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50), // 페이지 배경 Gray/50 BG (CSS)
        contentAlignment = Alignment.Center,
    ) {
        val report = uiState.detail
        when {
            uiState.isLoading -> CircularProgressIndicator(color = Primary600)

            uiState.errorMessage != null || report == null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.errorMessage ?: "리포트를 불러오지 못했어요.",
                        style = TqType.BodyM.figma(),
                        color = Error,
                    )
                    Spacer(Modifier.height(16.dp))
                    TqButton(text = "다시 시도", onClick = onRetry)
                }
            }

            // "리포트 저장하기"를 누르면 화면 위로 "저장됨" 시트가 올라온다. 성장 리포트와 같은
            // 공용 시트(ReportSaveSheetScaffold)이고, 카드 제목만 주차 범위로 채워진다.
            else -> ReportSaveSheetScaffold(
                savedReport = uiState.saveSheetReport,
                recentSavedReports = uiState.savedReports,
                onDismiss = onDismissSaveSheet,
                onToggleSave = onToggleSaveInSheet,
                onArchiveClick = onArchiveClick,
                onReportClick = { reportId ->
                    // 카드 종류에 따라 갈 상세 화면이 다르다. 시트 위 카드는 언제나 주간 비교.
                    val isWeekly = uiState.saveSheetReport?.id == reportId ||
                        uiState.savedReports.firstOrNull { it.id == reportId }?.type == "weekly_compare"
                    onReportClick(reportId, isWeekly)
                },
            ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
            ) {
                WeeklyCompareHeader(onClose = onClose)

                // 버튼은 화면 하단에 고정해 띄우고 본문만 스크롤되게 한다(사용자 결정).
                // 가림 마스크 없이 버튼 뒤로 본문 글자가 지나가는 걸 그대로 둔다.
                Box(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        WeekSwitcher(
                            prevWeekLabel = report.prevWeekLabel,
                            thisWeekLabel = report.thisWeekLabel,
                            canGoPrev = uiState.canGoPrev,
                            canGoNext = uiState.canGoNext,
                            dimPrev = uiState.dimPrev,
                            dimNext = uiState.dimNext,
                            onPrevWeek = onPrevWeek,
                            onNextWeek = onNextWeek,
                        )

                        Spacer(Modifier.height(16.dp)) // 427321812 gap

                        SectionTitle("지난 주보다 이만큼 달라졌어요", color = Gray800)
                        Spacer(Modifier.height(12.dp))
                        MetricGrid(report.metrics)

                        Spacer(Modifier.height(12.dp)) // 427321809 gap
                        HighlightCard(report.highlights)

                        Spacer(Modifier.height(18.dp)) // 427321810 gap
                        SectionTitle("자주 연습한 주제", color = Gray900)
                        Spacer(Modifier.height(12.dp))
                        TopicRow(report.topics)

                        Spacer(Modifier.height(18.dp)) // 427321811 gap
                        SectionTitle("지금까지 얼마나 달려왔을까요?", color = Gray900)
                        Spacer(Modifier.height(12.dp))
                        MissionProgressCard(
                            report = report,
                            onCompletedMissionsClick = onCompletedMissionsClick,
                        )

                        // 버튼이 스크롤 위에 고정으로 겹쳐 있어 이 여백이 없으면 마지막 카드가
                        // 버튼에 영구히 가려진다. 82 = 6(본문 끝→버튼 기존 간격) + 52(버튼 높이)
                        // + 24(버튼 하단 여백) — 끝까지 내렸을 때 원래 레이아웃과 같은 간격이 남는다.
                        Spacer(Modifier.height(82.dp))
                    }

                    TqButton(
                        text = "리포트 저장하기",
                        onClick = onSaveClick,
                        // 362 @ x16 → 좌 16 / 우 15 (CSS 값 그대로), 하단 24는 기존 Spacer 값 그대로
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 15.dp, bottom = 24.dp),
                    )
                }
            }
            }
        }
    }
}

// 헤더 — 닫기(0,40 44x44) + 가운데 제목. CSS는 255폭 프레임이라 제목 중심이 197(화면 중심 196.5).
@Composable
private fun WeeklyCompareHeader(onClose: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(44.dp)) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape) // 눌림 효과 원형 (아이콘 버튼 관례)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_weekly_close),
                contentDescription = "닫기",
                modifier = Modifier.size(14.dp),
            )
        }
        Text(
            text = "주간 비교 리포트",
            style = TqType.TitleL.figma(),
            color = Gray700,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

// 주차 이동 — 44 + [지난주 8 화살표 8 이번주] + 44 = 234, 가로 가운데 (CSS 427321808)
@Composable
private fun WeekSwitcher(
    prevWeekLabel: String,
    thisWeekLabel: String,
    canGoPrev: Boolean,
    canGoNext: Boolean,
    dimPrev: Boolean,
    dimNext: Boolean,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(44.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WeekArrow(
            iconRes = R.drawable.ic_weekly_prev,
            description = "이전 주차",
            enabled = canGoPrev,
            dimmed = dimPrev,
            onClick = onPrevWeek,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = prevWeekLabel, style = TqType.BodyM.figma(), color = Gray600)
            // CSS 프레임 18x18 안에 12.5x9 글리프 — export본(14x11)을 가운데 두고 자리는 18 유지
            Box(modifier = Modifier.size(18.dp), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.ic_weekly_range_arrow),
                    contentDescription = null,
                    modifier = Modifier.requiredSize(width = 14.dp, height = 11.dp),
                )
            }
            Text(text = thisWeekLabel, style = TqType.LabelL.figma(), color = Gray600)
        }
        WeekArrow(
            iconRes = R.drawable.ic_weekly_next,
            description = "다음 주차",
            enabled = canGoNext,
            dimmed = dimNext,
            onClick = onNextWeek,
        )
    }
}

@Composable
private fun WeekArrow(
    iconRes: Int,
    description: String,
    enabled: Boolean,
    dimmed: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = description,
            modifier = Modifier.size(width = 8.dp, height = 13.dp),
            // 기본은 시안대로 Purple/600. 주차가 여럿인데 끝에 닿았을 때만 흐리게 (사용자 결정)
            colorFilter = if (dimmed) ColorFilter.tint(Gray300) else null,
        )
    }
}

// 섹션 제목 — CSS상 프레임 padding 0 4px이라 글자 시작 x = 15 + 4 = 19
@Composable
private fun SectionTitle(text: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        text = text,
        style = TqType.BodyM.figma(),
        color = color,
        modifier = Modifier.padding(start = 19.dp),
    )
}

// 핵심 지표 2x2 — 카드 172 + gap 16 (행 폭 367이라 오른쪽 7 남는 것도 CSS 그대로)
@Composable
private fun MetricGrid(metrics: List<MetricChange>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        metrics.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 15.dp, end = 11.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                row.forEach { MetricCard(it) }
            }
        }
    }
}

@Composable
private fun MetricCard(metric: MetricChange) {
    val delta = metric.thisWeek - metric.lastWeek
    Column(
        modifier = Modifier
            .width(172.dp)
            .height(76.dp)
            .softShadow(
                color = Gray1000.copy(alpha = 0.01f),
                offsetY = 8.dp,
                blur = 24.dp,
                cornerRadius = 12.dp,
            )
            .background(White, RoundedCornerShape(12.dp))
            .padding(start = 16.dp), // CSS padding 0 0 0 16
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = metric.name, style = TqType.BodyM.figma(), color = Gray900)
        Spacer(Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "${metric.lastWeek} → ${metric.thisWeek}",
                style = TqType.BodyS.figma(),
                color = Gray400,
                // 시안 66~68. 점수가 네 자리여도 안 눌리게 최소폭으로 (사용자 결정)
                modifier = Modifier.widthIn(min = 68.dp),
            )
            MetricDeltaBadge(delta)
        }
    }
}

// 증감 배지 — 상승 보라(Purple/100 + Purple/700), 하락 회색(Gray/100 + Gray/500).
// 변화 없음(0)은 시안에 없어 하락 색을 쓰되 화살표를 빼고 숫자만 둔다 (사용자 결정).
@Composable
private fun MetricDeltaBadge(delta: Int) {
    val isUp = delta > 0
    Row(
        modifier = Modifier
            .height(22.dp)
            .background(if (isUp) Primary100 else Gray100, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (delta != 0) {
            // CSS 자리는 6x7, 글리프는 stroke 포함 9x10 — 자리는 유지하고 글리프만 키워 배치
            Box(
                modifier = Modifier.size(width = 6.dp, height = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(
                        if (isUp) R.drawable.ic_metric_up else R.drawable.ic_metric_down,
                    ),
                    contentDescription = if (isUp) "상승" else "하락",
                    modifier = Modifier.requiredSize(width = 9.dp, height = 10.dp),
                )
            }
        }
        Text(
            text = abs(delta).toString(),
            style = TqType.LabelL.figma(),
            color = if (isUp) Primary700 else Gray500,
        )
    }
}

// 개선 하이라이트 — 흰 카드 1장에 문장 여러 줄 (CSS 427321806).
// 높이는 CSS 76 고정이지만 서버 문장이 길면 잘려서 최소 높이로 뒀다 (사용자 결정).
@Composable
private fun HighlightCard(highlights: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 11.dp)
            .heightIn(min = 76.dp)
            .background(White, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        highlights.forEach { line ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(R.drawable.ic_highlight_sparkle),
                        contentDescription = null,
                        modifier = Modifier.requiredSize(18.dp),
                    )
                }
                Text(
                    text = line,
                    style = TqType.BodyM.figma().copy(lineBreak = PhraseFirst),
                    color = Gray600,
                )
            }
        }
    }
}

// 자주 연습한 주제 — 칩 89, gap 16. 화면 밖으로 이어지고 오른쪽 끝이 배경색으로 흐려진다.
@Composable
private fun TopicRow(topics: List<CategoryRank>) {
    Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 17.dp, end = 15.dp), // CSS 칩 시작 x = 17
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            itemsIndexed(topics) { index, topic -> TopicChip(rank = index + 1, topic = topic) }
        }
        // 그라데이션 마스크 378 @ x15, 84.13% 지점부터 배경색 (CSS)
        Box(
            modifier = Modifier
                .padding(start = 15.dp)
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0.8413f to Gray50.copy(alpha = 0f),
                            1f to Gray50,
                        ),
                    ),
                ),
        )
    }
}

@Composable
private fun TopicChip(rank: Int, topic: CategoryRank) {
    val isTop = rank == 1 // 1등만 보라 (CSS)
    Column(
        modifier = Modifier
            .width(89.dp)
            .height(100.dp)
            .softShadow(
                color = Primary600.copy(alpha = 0.04f),
                offsetX = 2.dp,
                offsetY = 4.dp,
                blur = 4.dp,
                cornerRadius = 12.dp,
            )
            .background(White, RoundedCornerShape(12.dp))
            .padding(top = 14.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(if (isTop) Primary100 else Gray100, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = rank.toString(),
                style = TqType.BodyM.figma(),
                color = if (isTop) Primary600 else Gray600,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = topic.name,
            // CSS 16/24 Medium — 이름 붙은 토큰이 없어 BodyL에 Medium을 얹음
            style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(),
            color = if (isTop) Primary600 else Gray600,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(2.dp))
        Text(text = "${topic.count}회", style = TqType.BodyS.figma(), color = Gray500)
    }
}

// 진행률 — 도넛(60, 두께 5) + 문구 + "완료한 미션 >" 칩 (CSS 427321096, radius 20)
@Composable
private fun MissionProgressCard(
    report: WeeklyCompareDetail,
    onCompletedMissionsClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 11.dp)
            .height(84.dp)
            .softShadow(
                color = Gray1000.copy(alpha = 0.01f),
                offsetY = 8.dp,
                blur = 24.dp,
                cornerRadius = 20.dp,
            )
            .background(White, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        ProgressDonut(percent = report.progressPercent)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "톡깨 미션 ${report.totalMissions}개 중, ${report.completedMissions}개 완료했어요!",
                style = TqType.BodyS.figma().copy(lineBreak = PhraseFirst),
                color = Gray800,
                modifier = Modifier.padding(horizontal = 2.dp), // CSS 427321163 padding 0 2
            )
            Row(
                modifier = Modifier
                    .height(26.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Gray100)
                    .clickable(onClick = onCompletedMissionsClick)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(text = "완료한 미션", style = TqType.LabelM.figma(), color = Gray600)
                Image(
                    painter = painterResource(R.drawable.ic_forward_chevron_small),
                    contentDescription = null,
                    modifier = Modifier.size(width = 16.dp, height = 17.dp),
                    colorFilter = ColorFilter.tint(Gray500),
                )
            }
        }
    }
}

// 도넛 60x60 — 바깥 60 / 안쪽 50이라 링 두께 5. 12시에서 시계방향.
@Composable
private fun ProgressDonut(percent: Int) {
    Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(60.dp)) {
            val stroke = 5.dp.toPx()
            val inset = stroke / 2f
            val arcSize = androidx.compose.ui.geometry.Size(
                size.width - stroke,
                size.height - stroke,
            )
            val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
            drawArc( // 트랙
                color = Gray200,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke),
            )
            drawArc( // 진행분
                color = Primary600,
                startAngle = -90f,
                sweepAngle = 360f * percent.coerceIn(0, 100) / 100f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke),
            )
        }
        Text(text = "$percent%", style = TqType.LabelL.figma(), color = Primary600)
    }
}

// 어절 단위로 끊되, 한 어절이 폭을 넘을 때만 어절 안에서 끊는다 (알림창·피드백 상세와 같은 규칙)
private val PhraseFirst = LineBreak(
    strategy = LineBreak.Strategy.Simple,
    strictness = LineBreak.Strictness.Normal,
    wordBreak = LineBreak.WordBreak.Phrase,
)

private val FullLeading = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private fun TextStyle.figma(): TextStyle = copy(lineHeightStyle = FullLeading)

// ── 프리뷰 ──

private val previewReport = WeeklyCompareDetail(
    prevWeekLabel = "7월 4주차",
    thisWeekLabel = "8월 1주차",
    metrics = listOf(
        MetricChange(name = "친절한 태도", lastWeek = 240, thisWeek = 300),
        MetricChange(name = "대화 주도", lastWeek = 240, thisWeek = 300),
        MetricChange(name = "공감 표현", lastWeek = 320, thisWeek = 310),
        MetricChange(name = "질문 연결성", lastWeek = 280, thisWeek = 310),
    ),
    highlights = listOf(
        "질문 연결성을 꾸준히 개선하고 있어요",
        "친절한 태도가 가장 많이 상승되었어요",
    ),
    topics = listOf(
        CategoryRank(name = "여행", count = 10),
        CategoryRank(name = "음식", count = 9),
        CategoryRank(name = "일상", count = 7),
        CategoryRank(name = "인사", count = 4),
    ),
    completedMissions = 26,
    totalMissions = 100,
)

@Preview(name = "주간 비교 리포트", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun WeeklyCompareScreenPreview() {
    TalkQQuestTheme {
        WeeklyCompareScreen(
            uiState = WeeklyCompareUiState(isLoading = false, detail = previewReport),
        )
    }
}
