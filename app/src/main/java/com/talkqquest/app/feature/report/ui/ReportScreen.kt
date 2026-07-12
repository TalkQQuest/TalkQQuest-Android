package com.talkqquest.app.feature.report.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.withStyle
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
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.Primary100
import com.talkqquest.app.core.designsystem.Primary50
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.Primary700
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.component.TqButton
import com.talkqquest.app.core.designsystem.softShadow
import com.talkqquest.app.feature.report.data.model.CategoryRank
import com.talkqquest.app.feature.report.data.model.GrowthReport
import com.talkqquest.app.feature.report.data.model.HighlightItem
import com.talkqquest.app.feature.report.data.model.MetricChange
import com.talkqquest.app.feature.report.data.model.WeeklyCompareReport
import com.talkqquest.app.feature.report.viewmodel.ReportUiState
import com.talkqquest.app.feature.report.viewmodel.ReportViewModel
import androidx.compose.ui.text.font.FontWeight

// ── 리포트 (CSS "성장 리포트"/"주간 비교 리포트" 프레임 전사 — 같은 탭바 공유라 1화면+2탭) ──
// 공통: 뒤로가기(0,48) + 탭바(66,69 260x33) / 성장: 레벨 비교 → 성장 추이 카드 → 카테고리 TOP → 완료 미션
// 주간: 헤더 배너(로봇) → 핵심 지표 변화 2x2 → 개선 하이라이트 3행
// CSS와 다른 점(합의됨):
//  - CSS의 하단 "다음" 버튼(362x52)은 행선지가 없어 제거 (홈 탭·뒤로가기로 충분 — 사용자 결정)
//  - 하단 네비 있음 + 홈 탭 보라 유지(홈 플로우 하위 화면 관례) — MainScreen/TqBottomBar에 등록
//  - 탭 전환 모션(세그먼트 슬라이드 + 콘텐츠 크로스페이드)은 디자인에 없어 자작
//  - 레벨 사이 화살표는 CSS상 이중 셰브런 아이콘 2개지만 export본(연보라오른쪽화살표.svg)이
//    셰브런 2개 한 묶음이라 export본 그대로 1개 배치 (디자이너 확인거리)

@Composable
fun ReportScreen(
    onBack: () -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel(),
    onSheetTopChange: (Float?) -> Unit = {}, // 저장 시트가 하단 네비를 덮는 동안 네비 가림
    // ── C담당(아카이브) 연결 지점 — 저장 시트 안에서 아카이브로 나가는 두 경로 ──
    onArchiveClick: () -> Unit = {}, // 시트 "보관함 >" → 아카이브 보관함(리포트 탭)
    onReportClick: (Long) -> Unit = {}, // 시트의 저장된 리포트 카드 → 보관함 리포트 상세
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ReportScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::loadReports,
        onSaveReport = viewModel::saveReport,
        onToggleReportSave = viewModel::toggleReportSave,
        onDismissSaveSheet = viewModel::dismissSaveSheet,
        onSheetTopChange = onSheetTopChange,
        onArchiveClick = onArchiveClick,
        onReportClick = onReportClick,
    )
}

@Composable
private fun ReportScreen(
    uiState: ReportUiState,
    onBack: () -> Unit = {},
    onRetry: () -> Unit = {},
    onSaveReport: () -> Unit = {},
    onToggleReportSave: (Long) -> Unit = {},
    onDismissSaveSheet: () -> Unit = {},
    onSheetTopChange: (Float?) -> Unit = {},
    onArchiveClick: () -> Unit = {},
    onReportClick: (Long) -> Unit = {},
) = FitDesign { // 작은 화면에선 디자인(393x852) 통째 축소 — 다른 화면들과 동일
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50), // 페이지 배경 Gray/50 BG (CSS)
        contentAlignment = Alignment.Center,
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator(color = Primary600)

            uiState.errorMessage != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = uiState.errorMessage, style = TqType.BodyM.figma(), color = Error)
                    Spacer(Modifier.height(16.dp))
                    TqButton(text = "다시 시도", onClick = onRetry)
                }
            }

            uiState.growth != null && uiState.weekly != null ->
                // "리포트 저장하기"를 누르면 화면 위로 "저장됨" 시트가 올라옴 (CSS "리포트 저장").
                // 표준 시트라 배경 안 어두워지고 뒤 화면도 계속 스크롤 가능 — 미션 저장 시트와 동일.
                ReportSaveSheetScaffold(
                    savedReport = uiState.saveSheetReport,
                    recentSavedReports = uiState.savedReports,
                    onDismiss = onDismissSaveSheet,
                    onToggleSave = onToggleReportSave,
                    onSheetTopChange = onSheetTopChange,
                    onArchiveClick = onArchiveClick,
                    onReportClick = onReportClick,
                ) {
                    ReportContent(
                        growth = uiState.growth,
                        weekly = uiState.weekly,
                        onBack = onBack,
                        onSaveClick = onSaveReport,
                    )
                }
        }
    }
}

@Composable
private fun ReportContent(
    growth: GrowthReport,
    weekly: WeeklyCompareReport,
    onBack: () -> Unit,
    onSaveClick: () -> Unit = {}, // 리포트 저장 → 시트 등장 (카드 제목은 이 리포트가 나온 미션명)
    initialTab: Int = 0, // 기본 = 성장 리포트 (CSS 프레임·탭바 순서)
) {
    var tab by rememberSaveable { mutableIntStateOf(initialTab) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Spacer(Modifier.height(8.dp)) // 상태바(40) → 헤더(top 48) (CSS)

        // 헤더: 뒤로가기 44(y 48~92) + 탭바(66,69 → 헤더 안 y 21) — 살짝 어긋난 배치가 CSS 값 그대로
        Box(modifier = Modifier.fillMaxWidth().height(54.dp)) {
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
                    tint = Gray500,
                )
            }
            ReportTabBar(
                tab = tab,
                onTabChange = { tab = it },
                modifier = Modifier.offset(x = 66.dp, y = 21.dp),
            )
        }

        // 탭 콘텐츠: 병렬 전환이라 크로스페이드 (하단 탭 전환과 같은 결 — NavGraph 관례)
        Box(modifier = Modifier.weight(1f)) {
            Crossfade(targetState = tab, animationSpec = tween(300), label = "reportTab") { current ->
                when (current) {
                    0 -> GrowthTab(growth)
                    else -> WeeklyCompareTab(weekly)
                }
            }
            // 리포트 저장하기 (CSS Frame 272: 두 탭 공통, top 660 절대 위치 = 탭바 끝 102부터 558.
            //  화면 밑 고정이 아니라 콘텐츠 기준 고정 — 콘텐츠와의 간격(성장 36/주간 14)이 CSS 그대로)
            // 누르면 저장 시트가 올라옴 (CSS "리포트 저장" 프레임 — 라벨은 CSS "다음" 대신
            // "리포트 저장하기" 유지, 사용자 결정). TODO(서버 연동): 저장 API(E102) 호출로 교체.
            TqButton(
                text = "리포트 저장하기",
                onClick = onSaveClick,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 558.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
        }

    }
}

// 탭바: 260x33 흰 알약(r8) 안에서 보라(Purple700) 세그먼트가 슬라이드 (CSS 리포트 프레임)
@Composable
private fun ReportTabBar(
    tab: Int,
    onTabChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val indicatorX by animateDpAsState(
        targetValue = if (tab == 0) 0.dp else 130.dp,
        animationSpec = tween(250),
        label = "tabIndicator",
    )
    Box(
        modifier = modifier
            .size(width = 260.dp, height = 33.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(White),
    ) {
        Box(
            modifier = Modifier
                .offset(x = indicatorX)
                .size(width = 130.dp, height = 33.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Primary700),
        )
        Row(Modifier.fillMaxSize()) {
            listOf("성장 리포트", "주간 비교 리포트").forEachIndexed { index, label ->
                val textColor by animateColorAsState(
                    targetValue = if (tab == index) Gray50 else Gray600,
                    animationSpec = tween(250),
                    label = "tabText",
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null, // 세그먼트 슬라이드가 곧 피드백 — 물결 없음
                        ) { onTabChange(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = label, style = TqType.LabelL.figma(), color = textColor)
                }
            }
        }
    }
}

// ── 성장 리포트 탭 ──

@Composable
private fun GrowthTab(growth: GrowthReport) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp)) // 탭바 끝(102) → 레벨 비교(126) (CSS)
        LevelCompareRow(prevLevel = growth.prevLevel, currentLevel = growth.currentLevel)
        Spacer(Modifier.height(24.dp)) // 레벨 비교 끝(186) → 콘텐츠(210) (CSS)
        GrowthTrendCard(
            growthPercent = growth.growthPercent,
            weekLabels = growth.weekLabels,
            modifier = Modifier.padding(horizontal = 15.dp), // 콘텐츠 열 left 15 / w363 (CSS)
        )
        Spacer(Modifier.height(24.dp)) // 열 gap 24 (CSS)
        CategoryRankRow(ranks = growth.categoryRanks)
        Spacer(Modifier.height(24.dp))
        CompletedMissionCard(
            completed = growth.completedMissions,
            total = growth.totalMissions,
            modifier = Modifier.padding(horizontal = 15.dp),
        )
    }
}

// 저번 주 Lv.N → 이번 주 Lv.M (CSS Frame 427321109: 254x60 가운데)
@Composable
private fun LevelCompareRow(prevLevel: Int, currentLevel: Int) {
    Row(verticalAlignment = Alignment.Bottom) {
        LevelChip(label = "저번 주", level = prevLevel, highlighted = false)
        Spacer(Modifier.width(8.dp)) // 열 gap 8 (CSS)
        Image(
            painter = painterResource(R.drawable.ic_report_level_arrow),
            contentDescription = null,
            modifier = Modifier.size(width = 42.dp, height = 36.dp),
        )
        Spacer(Modifier.width(8.dp))
        LevelChip(label = "이번 주", level = currentLevel, highlighted = true)
    }
}

@Composable
private fun LevelChip(label: String, level: Int, highlighted: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = TqType.LabelM.figma(),
            color = if (highlighted) Gray600 else Gray500, // 이번 주만 한 톤 진하게 (CSS)
        )
        Spacer(Modifier.height(6.dp)) // 라벨 → 칩 gap 6 (CSS)
        Box(
            modifier = Modifier
                .size(width = 98.dp, height = 36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (highlighted) Primary100 else Gray100),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Lv.$level",
                style = TqType.TitleL.figma(),
                color = if (highlighted) Primary600 else Gray400,
            )
        }
    }
}

// 대화 성장 추이 카드 (CSS Frame 427321092: 363x182, 흰 r20 + 카드 그림자)
@Composable
private fun GrowthTrendCard(
    growthPercent: Int,
    weekLabels: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = "대화 성장 추이", style = TqType.LabelL.figma(), color = Gray800)
            Text(text = "+ $growthPercent%", style = TqType.LabelL.figma(), color = Primary600)
        }
        Spacer(Modifier.height(8.dp)) // 카드 안 gap 8 (CSS)
        GrowthChart()
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Gray300)) // 구분선 (CSS)
        Spacer(Modifier.height(6.dp)) // 구분선 → 라벨 gap 6 (CSS)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            weekLabels.forEach { label ->
                Text(text = label, style = TqType.Caption.figma(), color = Gray400)
            }
        }
    }
}

// 라인 차트 327x97 — 선·점 좌표는 차트.svg와 CSS 값 그대로 전사.
// TODO(서버 연동): 주차별 점수 데이터가 오면 좌표를 값에서 계산하도록 교체 — 지금은 목업 고정(사용자 결정).
@Composable
private fun GrowthChart() {
    // 꺾은선 꼭짓점 (차트.svg 좌표 + CSS Vector 57 위치 y+34.09)
    val linePoints = listOf(
        Offset(0.00f, 87.00f),
        Offset(121.71f, 57.00f),
        Offset(209.09f, 77.50f),
        Offset(304.80f, 35.00f),
    )
    // 중간 점 3개: 중심 좌표 (CSS Frame 427321086~88 + 반지름 4)
    val dots = listOf(
        Offset(32.09f, 79.00f),
        Offset(121.55f, 57.00f),
        Offset(209.97f, 77.00f),
    )
    val endDot = Offset(304.60f, 35.00f) // 마지막 점 (10px + 글로우)
    val glowColor = Primary600.copy(alpha = 0.45f).toArgb()

    Canvas(modifier = Modifier.size(width = 327.dp, height = 97.dp)) {
        val s = size.width / 327f // dp 프레임 → px 배율

        // 뒷 배경: 선 아래 영역을 채우고 아래로 사라지는 보라 그라데이션
        // (피그마 렌더 = 영역 차트 모양. 색 스톱은 CSS '뒷 배경' 그라데이션 값 환산, 아래끝 y=97)
        val areaPath = Path().apply {
            linePoints.forEachIndexed { index, p ->
                if (index == 0) moveTo(p.x * s, p.y * s) else lineTo(p.x * s, p.y * s)
            }
            lineTo(linePoints.last().x * s, 97f * s)
            lineTo(0f, 97f * s)
            close()
        }
        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                0.0000f to Primary600.copy(alpha = 0.0871f),
                0.2291f to Primary600.copy(alpha = 0.06f),
                0.9887f to Primary600.copy(alpha = 0f),
                startY = 35f * s,
                endY = 97f * s,
            ),
        )

        // 꺾은선 (Purple/600, 2px)
        val path = Path().apply {
            linePoints.forEachIndexed { index, p ->
                if (index == 0) moveTo(p.x * s, p.y * s) else lineTo(p.x * s, p.y * s)
            }
        }
        drawPath(path = path, color = Primary600, style = Stroke(width = 2f * s))

        // 중간 점: 보라 8px + 속 Purple/50 4px
        dots.forEach { c ->
            drawCircle(color = Primary600, radius = 4f * s, center = Offset(c.x * s, c.y * s))
            drawCircle(color = Primary50, radius = 2f * s, center = Offset(c.x * s, c.y * s))
        }

        // 마지막 점: 글로우(drop-shadow 0 0 4 보라 45%) + 보라 10px + 속 6px
        // 글로우 반지름 = 점과 동일 5 → 블러가 무시되는 구형(API<28)에선 점에 가려져 티 안 남
        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
                color = glowColor
                maskFilter = android.graphics.BlurMaskFilter(4f * s, android.graphics.BlurMaskFilter.Blur.NORMAL)
            }
            canvas.nativeCanvas.drawCircle(endDot.x * s, endDot.y * s, 5f * s, paint)
        }
        drawCircle(color = Primary600, radius = 5f * s, center = Offset(endDot.x * s, endDot.y * s))
        drawCircle(color = Primary50, radius = 3f * s, center = Offset(endDot.x * s, endDot.y * s))
    }
}

// 카테고리 TOP 가로 리스트 (CSS Frame 427321126: 카드 89x100, 넘친 만큼 가로 스크롤 + 오른끝 페이드)
@Composable
private fun CategoryRankRow(ranks: List<CategoryRank>) {
    Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp), // 카드 gap 16 (CSS)
            contentPadding = PaddingValues(horizontal = 17.dp),  // 첫 카드 left 17 (CSS 15+2)
        ) {
            itemsIndexed(ranks) { index, rank ->
                RankCard(rank = rank, order = index + 1)
            }
        }
        // 그라데이션 마스크: 오른끝에서 배경색으로 사라짐 (CSS 84.13%→100%, 378px 중 약 60px 구간)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(60.dp)
                .background(Brush.horizontalGradient(0f to Gray50.copy(alpha = 0f), 1f to Gray50)),
        )
    }
}

@Composable
private fun RankCard(rank: CategoryRank, order: Int) {
    val top = order == 1 // 1위만 보라 강조 (CSS)
    Column(
        modifier = Modifier
            .size(width = 89.dp, height = 100.dp)
            .softShadow(color = Primary600.copy(alpha = 0.04f), offsetX = 2.dp, offsetY = 4.dp, blur = 4.dp, cornerRadius = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(White)
            .padding(top = 14.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (top) Primary100 else Gray100),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$order",
                style = TqType.BodyM.figma(),
                color = if (top) Primary600 else Gray600,
            )
        }
        Spacer(Modifier.height(8.dp)) // 순위 → 이름 gap 8 (CSS)
        Text(
            text = rank.name,
            // CSS: 16px/24 Medium (토큰 밖 스타일 — BodyL에 Medium만 얹음)
            style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(),
            color = if (top) Primary600 else Gray600,
        )
        Spacer(Modifier.height(2.dp)) // 이름 → 횟수 gap 2 (CSS)
        Text(text = "${rank.count}회", style = TqType.BodyS.figma(), color = Gray500)
    }
}

// 완료한 미션 카드 (CSS Frame 427321096: 363x84, 도넛 26% + 문구 + 칩)
@Composable
private fun CompletedMissionCard(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    val percent = if (total > 0) completed * 100 / total else 0
    Row(
        modifier = modifier
            .fillMaxWidth()
            .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 도넛: 회색 링(두께 5) 위에 보라 호 — 12시부터 시계방향 percent 만큼 (CSS Subtract 모양)
        Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = Stroke(width = size.width * 5f / 60f)
                val inset = stroke.width / 2f
                drawCircle(
                    color = Gray200,
                    radius = size.width / 2f - inset,
                    style = stroke,
                )
                drawArc(
                    color = Primary600,
                    startAngle = -90f,
                    sweepAngle = percent * 3.6f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2f, size.height - inset * 2f),
                    style = stroke,
                )
            }
            Text(text = "$percent%", style = TqType.LabelL.figma(), color = Primary600)
        }
        Spacer(Modifier.width(24.dp)) // 도넛 → 텍스트 gap 24 (CSS)
        Column {
            Text(
                text = "톡깨 미션 ${total}개 중, ${completed}개 완료했어요!",
                style = TqType.BodyS.figma(),
                color = Gray900,
            )
            Spacer(Modifier.height(8.dp)) // 문구 → 칩 gap 8 (CSS)
            // TODO(추후): 아카이브 완료 미션 목록(A담당) 생기면 클릭 연결 — 지금은 표시만.
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Gray100)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "완료한 미션", style = TqType.LabelM.figma(), color = Gray600)
                Spacer(Modifier.width(2.dp)) // 글자 → 화살표 gap 2 (CSS)
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Gray500,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

// ── 주간 비교 리포트 탭 ──

@Composable
private fun WeeklyCompareTab(weekly: WeeklyCompareReport) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(18.dp)) // 탭바 끝(102) → 배너(120) (CSS)

        // 헤더 배너: 화면 전체 폭 Purple/50 (CSS 393x90, r8 — 높이 86 → 90 디자인 변경 2026-07)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Primary50)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    // 피그마 렌더: 뒤 구절만 보라 강조 (CSS 추출본엔 단색만 나와 렌더 기준)
                    text = buildAnnotatedString {
                        append("지난주보다 ")
                        withStyle(SpanStyle(color = Primary600)) { append("이렇게 달라졌어요") }
                    },
                    style = TqType.TitleL.figma(),
                    color = Gray800,
                )
                Spacer(Modifier.height(2.dp)) // 제목 → 부제 gap 2 (CSS)
                Text(
                    // CSS 고정 폭 144의 줄바꿈 위치를 그대로 — 폭 제약으로 재면 "이번/주의"로 갈라져서 명시
                    text = "4가지 핵심 지표를 비교해\n이번 주의 변화를 확인해보세요",
                    style = TqType.Caption.figma(),
                    color = Gray500,
                )
            }
            // 로봇: PNG(336x258)의 실제 로봇은 바닥 여백 ~0·상단 여백 9%. 폭만 지정하고 높이는
            // PNG 원비율(1.302)로 두면 레터박스가 안 생겨, 바닥 정렬 시 로봇 바닥이 배너 바닥에 딱 붙음.
            // (세로 고정 박스+Fit은 위아래 18dp 투명 레터박스가 생겨 로봇이 떠 보였음 — 크기 무관 버그였음)
            // CSS drop-shadow(0 0 48 보라 20%) = 로봇 실루엣 글로우 → 같은 이미지를 보라 틴트+블러로
            // 뒤에 깔아 재현 (API 31+만; 미만은 blur 무시돼 잔상이라 생략).
            Box(modifier = Modifier.align(Alignment.Bottom)) {
                if (android.os.Build.VERSION.SDK_INT >= 31) {
                    Image(
                        painter = painterResource(R.drawable.img_report_robot),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Primary600.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .width(104.86.dp)
                            .blur(48.dp, BlurredEdgeTreatment.Unbounded),
                    )
                }
                Image(
                    painter = painterResource(R.drawable.img_report_robot),
                    contentDescription = null,
                    modifier = Modifier.width(104.86.dp),
                )
            }
        }

        Spacer(Modifier.height(16.dp)) // 배너 끝 → 콘텐츠 간격 18 → 16 (디자인 변경 2026-07)
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(text = "핵심 지표 변화", style = TqType.LabelL.figma(), color = Gray800)
            Spacer(Modifier.height(16.dp)) // 제목 → 카드 gap 16 (CSS)
            // 2x2 지표 카드 (행 안 gap 16 / 행 사이 gap 12)
            val metrics = weekly.metrics
            metrics.chunked(2).forEachIndexed { rowIndex, rowMetrics ->
                if (rowIndex > 0) Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    rowMetrics.forEachIndexed { colIndex, metric ->
                        MetricCard(
                            metric = metric,
                            index = rowIndex * 2 + colIndex,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp)) // 지표 → 하이라이트 섹션 gap 16 (CSS)
            Text(text = "개선 하이라이트", style = TqType.LabelL.figma(), color = Gray800)
            weekly.highlights.forEach { highlight ->
                Spacer(Modifier.height(16.dp)) // 섹션 안 gap 16 (CSS)
                HighlightRow(item = highlight)
            }
        }
    }
}

// 지표 아이콘 (PNG = 피그마에서 카드에 잘린 "보이는 영역" export — 회전·잘림이 이미 구워져 있음.
// 크기 = CSS 기하(이미지 레이어 위치·회전 bbox를 카드 173x76으로 클립)로 계산한 보이는 영역.
// 잘린 면이 카드 오른쪽·아래 모서리라 BottomEnd 밀착 배치 — 코드 회전 없음)
private data class MetricIconSpec(val resId: Int, val width: Float, val height: Float)

private val metricIconSpecs = listOf(
    MetricIconSpec(R.drawable.img_report_kindness, width = 67f, height = 70f),       // 친절한 태도 (손하트)
    MetricIconSpec(R.drawable.img_report_initiative, width = 73.9f, height = 72.9f), // 대화 주도 (마이크)
    MetricIconSpec(R.drawable.img_report_empathy, width = 69.6f, height = 64.6f),    // 공감 표현 (하트)
    MetricIconSpec(R.drawable.img_report_question, width = 74.9f, height = 70.9f),   // 질문 연결성 (물음표)
)

// 핵심 지표 카드 (CSS Frame 427321132~135: 173x76, 아이콘이 카드 밖으로 넘쳐 잘림)
@Composable
private fun MetricCard(
    metric: MetricChange,
    index: Int,
    modifier: Modifier = Modifier,
) {
    val icon = metricIconSpecs.getOrNull(index)
    Box(
        modifier = modifier
            .height(76.dp)
            .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 12.dp)
            .clip(RoundedCornerShape(12.dp)) // 아이콘 넘침을 카드 모양대로 잘라냄 (피그마와 동일)
            .background(White),
    ) {
        if (icon != null) {
            Image(
                painter = painterResource(icon.resId),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(width = icon.width.dp, height = icon.height.dp),
            )
        }
        Column(modifier = Modifier.offset(x = 14.dp, y = 15.dp)) {
            Text(text = metric.name, style = TqType.BodyM.figma(), color = Color.Black)
            Spacer(Modifier.height(4.dp)) // 이름 → 점수 gap 4 (CSS)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${metric.lastWeek} → ${metric.thisWeek}",
                    style = TqType.Caption.figma(),
                    color = Gray400,
                )
                Spacer(Modifier.width(6.dp)) // 점수 → 배지 gap 6 (CSS)
                val diff = metric.thisWeek - metric.lastWeek
                Box(
                    modifier = Modifier
                        .size(width = 38.dp, height = 20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Primary100),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (diff >= 0) "+ $diff" else "- ${-diff}",
                        style = TqType.BodyS.figma(),
                        color = Primary700,
                    )
                }
            }
        }
    }
}

// 개선 하이라이트 행 (CSS Frame 427321150: 363x46, Gray100 r12 — 문구가 길면 아래로 늘어남)
@Composable
private fun HighlightRow(item: HighlightItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Gray100)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(R.drawable.img_report_star),
                contentDescription = null,
                modifier = Modifier.size(16.dp), // 20 박스 안 16 아이콘 (CSS padding 2)
            )
        }
        Spacer(Modifier.width(10.dp)) // 별 → 문구 gap 10 (CSS)
        Text(
            // 피그마 렌더: 앞 키워드만 보라, 나머지 검정 (CSS 추출본엔 단색으로 나와 렌더 기준)
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = Primary600)) { append(item.emphasis) }
                append(item.rest)
            },
            style = TqType.BodyM.figma(),
            color = Color.Black,
        )
    }
}

// 리포트 텍스트는 이걸로 감싸 피그마 line-height 여백을 살림 (홈/미션과 동일한 로컬 관례)
private val FullLeading = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private fun TextStyle.figma(): TextStyle = copy(lineHeightStyle = FullLeading)

// ── 프리뷰 ──

private val previewGrowth = GrowthReport(
    prevLevel = 1,
    currentLevel = 2,
    growthPercent = 18,
    weekLabels = listOf("7월 4주", "8월 1주", "8월 2주", "8월 3주"),
    categoryRanks = listOf(
        CategoryRank("여행", 10),
        CategoryRank("음식", 9),
        CategoryRank("일상", 7),
        CategoryRank("인사", 4),
    ),
    completedMissions = 26,
    totalMissions = 100,
)

private val previewWeekly = WeeklyCompareReport(
    metrics = listOf(
        MetricChange("친절한 태도", 88, 92),
        MetricChange("대화 주도", 86, 88),
        MetricChange("공감 표현", 82, 85),
        MetricChange("질문 연결성", 74, 78),
    ),
    highlights = listOf(
        HighlightItem("전체 점수", "가 78점에서 86점으로 상승했어요"),
        HighlightItem("친절한 태도", "가 가장 많이 상승되었어요"),
        HighlightItem("질문 연결성", "을 꾸준히 개선하고 있어요"),
    ),
)

@Preview(name = "성장 리포트 393dp", widthDp = 393, heightDp = 852, showBackground = true, backgroundColor = 0xFFF8FAFC)
@Preview(name = "성장 리포트 360dp", widthDp = 360, heightDp = 800, showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun GrowthReportPreview() {
    TalkQQuestTheme {
        ReportContent(growth = previewGrowth, weekly = previewWeekly, onBack = {})
    }
}

@Preview(name = "주간 비교 393dp", widthDp = 393, heightDp = 852, showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun WeeklyComparePreview() {
    TalkQQuestTheme {
        ReportContent(growth = previewGrowth, weekly = previewWeekly, onBack = {}, initialTab = 1)
    }
}
