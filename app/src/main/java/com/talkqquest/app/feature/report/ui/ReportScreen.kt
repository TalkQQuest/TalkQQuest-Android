package com.talkqquest.app.feature.report.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.Error
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray1000
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Primary200
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.softShadow
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.component.TqButton
import com.talkqquest.app.feature.report.data.model.Competency
import com.talkqquest.app.feature.report.data.model.CompetencyAxis
import com.talkqquest.app.feature.report.data.model.GrowthTierReport
import com.talkqquest.app.feature.report.viewmodel.ReportUiState
import com.talkqquest.app.feature.report.viewmodel.ReportViewModel

// ── 성장 리포트 (CSS UI-14 "성장 리포트" 프레임 전사 — 성장 티어 시스템 시각화) ──
// 실전 티어 카드(티어 휘장 + 별) + 핵심 역량 카드(마름모 4축 레이더 + 4행 범례).
// 주간 비교 리포트는 홈 알림 → 모달/보관함으로 빠져 이 화면에선 탭 없이 단독.
// 저장 시트(ReportSaveSheet)는 성장·주간 저장을 모두 담당 — 그대로 유지.
// 실전 티어 info 아이콘 → "티어 승급 안내" 바텀시트.

private val StarYellow = Color(0xFFF9AC17) // YELLOW_star

private val FullLeading = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)
private fun TextStyle.figma(): TextStyle = copy(lineHeightStyle = FullLeading)

// 티어 이름 → 성장 리포트용 소형(s) 휘장 리소스
private fun tierEmblemSmallRes(name: String): Int = when (name) {
    "브론즈" -> R.drawable.img_tier_bronze_s
    "실버" -> R.drawable.img_tier_silver_s
    "골드" -> R.drawable.img_tier_gold_s
    "플래티넘" -> R.drawable.img_tier_platinum_s
    "다이아" -> R.drawable.img_tier_dia_s
    "마스터" -> R.drawable.img_tier_master_s
    else -> R.drawable.img_tier_gold_s
}

@Composable
fun ReportScreen(
    onBack: () -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel(),
    onSheetTopChange: (Float?) -> Unit = {}, // 저장 시트가 하단 네비를 덮는 동안 네비 가림
    // ── C담당(아카이브) 연결 지점 — 저장 시트 안에서 아카이브로 나가는 두 경로 ──
    onArchiveClick: () -> Unit = {}, // 시트 "보관함 >" → 아카이브 보관함(리포트 탭)
    onReportClick: (String) -> Unit = {}, // 시트의 저장된 리포트 카드 → 보관함 리포트 상세
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
    onSaveReport: (String) -> Unit = {},
    onToggleReportSave: (String) -> Unit = {},
    onDismissSaveSheet: () -> Unit = {},
    onSheetTopChange: (Float?) -> Unit = {},
    onArchiveClick: () -> Unit = {},
    onReportClick: (String) -> Unit = {},
) = FitDesign { // 작은 화면에선 디자인(393x852) 통째 축소 — 다른 화면들과 동일
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50), // 페이지 배경 Gray/50 BG
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

            uiState.growth != null ->
                // "리포트 저장하기"를 누르면 화면 위로 "저장됨" 시트가 올라옴. 표준 시트라 배경 안 어두워지고
                // 뒤 화면도 계속 스크롤 가능 — 미션 저장 시트와 동일. 시트는 성장·주간 저장 공용.
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
                        onBack = onBack,
                        onSaveClick = onSaveReport,
                    )
                }
        }
    }
}

@Composable
private fun ReportContent(
    growth: GrowthTierReport,
    onBack: () -> Unit,
    onSaveClick: (String) -> Unit = {},
) {
    // info 아이콘 → 티어 승급 안내 바텀시트
    var showTierHelp by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Spacer(Modifier.height(8.dp)) // 상태바(40) → 헤더(top 48)

            // 헤더: 뒤로가기(left 0) + "성장 리포트"(중앙)
            Box(modifier = Modifier.fillMaxWidth().height(44.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back_chevron),
                        contentDescription = "뒤로가기",
                        tint = Gray500,
                    )
                }
                Text(
                    text = "성장 리포트",
                    style = TqType.TitleL.figma(), // 18/600
                    color = Gray700,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            Spacer(Modifier.height(5.dp)) // 헤더 끝(92) → 콘텐츠 top 97

            // 카드 묶음 (left 16, gap 13)
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                TierCard(
                    tierName = growth.tierName,
                    tierStars = growth.tierStars,
                    nextStarsNeeded = growth.nextStarsNeeded,
                    nextTierName = growth.nextTierName,
                    onInfoClick = { showTierHelp = true },
                )
                Spacer(Modifier.height(13.dp))
                CompetencyCard(competencies = growth.competencies)
            }

            Spacer(Modifier.height(9.dp)) // 카드 끝 → 버튼 top 728

            // 리포트 저장하기 (CSS "다음" 버튼 자리 — 라벨은 이전 사용자 결정대로 유지). 누르면 저장 시트.
            TqButton(
                text = "리포트 저장하기",
                onClick = { onSaveClick("growth") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
        }

        // 티어 승급 안내 바텀시트
        if (showTierHelp) {
            TierPromotionSheet(
                tierName = growth.tierName,
                competencies = growth.competencies,
                onDismiss = { showTierHelp = false },
            )
        }
    }
}

// ── 실전 티어 카드 (361x125) ──
@Composable
private fun TierCard(
    tierName: String,
    tierStars: Int,
    nextStarsNeeded: Int,
    nextTierName: String,
    onInfoClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .padding(start = 16.dp, top = 12.dp, bottom = 12.dp),
    ) {
        // "실전 티어" + info
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "실전 티어", style = TqType.LabelL.figma(), color = Gray500) // 14/500
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onInfoClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_notification_info),
                    contentDescription = "실전 티어 안내",
                    tint = Gray400, // information-circle Gray/400
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        // 휘장 + (티어명 / 별 3)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(tierEmblemSmallRes(tierName)),
                contentDescription = null,
                modifier = Modifier.size(55.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = tierName,
                    style = TqType.BodyL.figma().copy(fontWeight = FontWeight.Medium), // 16/500
                    color = Gray800,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) { i ->
                        Icon(
                            painter = painterResource(R.drawable.ic_tier_star),
                            contentDescription = null,
                            tint = if (i < tierStars) StarYellow else Gray300,
                            modifier = Modifier.size(15.dp),
                        )
                    }
                }
            }
        }

        // "별 N개를 더 획득하면 X이에요!"
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = Gray600)) { append("별 ") }
                withStyle(SpanStyle(color = Primary600, fontWeight = FontWeight.Medium)) { append("${nextStarsNeeded}개") }
                withStyle(SpanStyle(color = Gray600)) { append("를 더 획득하면 ") }
                withStyle(SpanStyle(color = Primary600, fontWeight = FontWeight.Medium)) { append(nextTierName) }
                withStyle(SpanStyle(color = Gray600)) { append("이에요!") }
            },
            style = TqType.BodyM.figma(), // 14/400
        )
    }
}

// ── 핵심 역량 카드 (361x484) ──
@Composable
private fun CompetencyCard(competencies: List<Competency>) {
    val byAxis = competencies.associateBy { it.axis }
    val top = byAxis[CompetencyAxis.KINDNESS]
    val right = byAxis[CompetencyAxis.INITIATIVE]
    val bottom = byAxis[CompetencyAxis.EMPATHY]
    val left = byAxis[CompetencyAxis.QUESTION_LINK]

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 제목
        Text(
            text = "핵심 역량",
            style = TqType.BodyL.figma().copy(fontWeight = FontWeight.Medium), // 16/500
            color = Color.Black,
            modifier = Modifier.padding(start = 16.dp),
        )

        // 레이더 + 4축 라벨 (위=친절/왼=질문/오른=주도/아래=공감)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (top != null) AxisLabel(top.label, top.gain)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (left != null) AxisLabel(left.label, left.gain)
                RadarChart(
                    top = frac(top),
                    right = frac(right),
                    bottom = frac(bottom),
                    left = frac(left),
                    modifier = Modifier.size(176.dp),
                )
                if (right != null) AxisLabel(right.label, right.gain)
            }
            if (bottom != null) AxisLabel(bottom.label, bottom.gain)
        }

        // 범례 4행
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            competencies.forEach { c -> LegendRow(c) }
        }
    }
}

private fun frac(c: Competency?): Float =
    if (c == null || c.maxScore == 0) 0f else (c.score.toFloat() / c.maxScore).coerceIn(0f, 1f)

// 레이더 축 라벨: 라벨 + "+획득" (세로)
@Composable
private fun AxisLabel(label: String, gain: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = TqType.BodyM.figma(), color = Gray800) // 14/400
        Text(text = "+$gain", style = TqType.LabelL.figma(), color = Primary600) // 14/500
    }
}

// 마름모 4축 레이더. 값(0..1)에 비례해 중심에서 각 축(위/오/아/왼)으로 뻗은 폴리곤.
@Composable
private fun RadarChart(
    top: Float,
    right: Float,
    bottom: Float,
    left: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.width / 2f - 4.dp.toPx() // 격자 마름모 반경(테두리 여백)
        val dot = size.width * 0.028f // 꼭짓점 점 반경(176일 때 ≈5)

        // 격자 (Gray/300) — Vector.svg 그대로: 동심 마름모 4겹(비율 1/0.75/0.54/0.29) + 수직·수평 축선
        val gridStroke = Stroke(width = 1.dp.toPx())
        listOf(1f, 0.752f, 0.540f, 0.293f).forEach { ring ->
            val rr = r * ring
            val diamond = Path().apply {
                moveTo(cx, cy - rr); lineTo(cx + rr, cy); lineTo(cx, cy + rr); lineTo(cx - rr, cy); close()
            }
            drawPath(diamond, color = Gray300, style = gridStroke)
        }
        drawLine(Gray300, Offset(cx, cy - r), Offset(cx, cy + r), strokeWidth = 1.dp.toPx()) // 수직 축선
        drawLine(Gray300, Offset(cx - r, cy), Offset(cx + r, cy), strokeWidth = 1.dp.toPx()) // 수평 축선

        // 데이터 폴리곤 (Purple/200 채움 + Purple/600 선)
        val pTop = Offset(cx, cy - r * top)
        val pRight = Offset(cx + r * right, cy)
        val pBottom = Offset(cx, cy + r * bottom)
        val pLeft = Offset(cx - r * left, cy)
        val data = Path().apply {
            moveTo(pTop.x, pTop.y); lineTo(pRight.x, pRight.y)
            lineTo(pBottom.x, pBottom.y); lineTo(pLeft.x, pLeft.y); close()
        }
        // Vector 78: Purple/200 채움 + Purple/600 선, 요소 전체 opacity 0.6 → 안쪽 격자가 비쳐 보임
        drawPath(data, color = Primary200.copy(alpha = 0.6f))
        drawPath(data, color = Primary600.copy(alpha = 0.6f), style = Stroke(width = 1.dp.toPx()))

        // 꼭짓점 점 (Purple/600)
        listOf(pTop, pRight, pBottom, pLeft).forEach { drawCircle(Primary600, radius = dot, center = it) }
    }
}

// 범례 행: [체크닷 + 라벨] ... [점수 / 만점]
@Composable
private fun LegendRow(c: Competency) {
    val maxed = c.score >= c.maxScore
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // 만점 달성 = Purple 채움 + 흰 체크 / 미달 = 빈 원(Gray/300 테두리, 체크 없음)
            if (maxed) {
                Box(
                    modifier = Modifier.size(19.dp).clip(CircleShape).background(Primary600),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_benefit_check),
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(13.dp),
                    )
                }
            } else {
                Box(modifier = Modifier.size(19.dp).clip(CircleShape).border(1.dp, Gray300, CircleShape))
            }
            Text(text = c.legendLabel, style = TqType.BodyM.figma(), color = Gray800) // 14/400
        }
        // 점수 / 만점 — 슬래시 양쪽 균등 간격
        Text(
            text = buildAnnotatedString {
                append(c.score.toString())
                withStyle(SpanStyle(color = Gray400, fontWeight = FontWeight.Normal)) { append(" / ${c.maxScore}") }
            },
            style = TqType.LabelL.figma(), // 14/500 (점수), 슬래시부는 span으로 400
            color = if (maxed) Primary600 else Gray600,
        )
    }
}

// ── 티어 승급 안내 바텀시트 (info 아이콘 → 열림) ──
@Composable
private fun TierPromotionSheet(
    tierName: String,
    competencies: List<Competency>,
    onDismiss: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 배경 딤 없음(피그마에 없음). 투명 클릭 영역으로 바깥 탭 시 닫기만.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        )
        // 시트 본체 — CSS "바텀시트": 393폭, Gray/50 BG, radius 36 top, padding 20/16, gap 20, height 342
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                // 시트 그림자 = CSS `0px -8px 24px rgba(15,23,42,0.06)` 단일 레이어(저장 시트와 동일).
                // 넓게 은은하게 페이드아웃 — 경계에 선 긋는 접촉 겹 없음.
                .softShadow(
                    color = Gray1000.copy(alpha = 0.06f),
                    offsetY = (-8).dp,
                    blur = 24.dp,
                    cornerRadius = 36.dp,
                )
                .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
                .background(Gray50)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp), // 핸들 ↔ 콘텐츠
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 핸들 Frame 453 (36x4, Gray/600)
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Gray600),
            )

            // 콘텐츠 Frame 427321757 (width 361, gap 16, align center)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "티어 승급 안내",
                    style = TqType.TitleL.figma(), // 18/600
                    color = Gray800,
                    // 제목은 가운데 정렬 (부모 CenterHorizontally + hug)
                )

                // 항목 Frame 427321756 (내용폭만큼 hug → 시트 가운데로 들여쓰기, gap 7)
                // 항목 · 가운데 chevron · 항목 · chevron · 항목. 행은 블록 안 좌측 정렬(아이콘 정렬),
                // chevron은 블록 폭 기준 가운데. 부제는 hug라 한 줄로 안 잘림.
                Column(
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    HelpRow(
                        title = "핵심 역량 채우기",
                        subtitle = "미션을 통해 대화 역량을 넓혀요",
                    ) {
                        // 미니 마름모 = Vector 78 이미지 (사용자 지정) — 50x50
                        Box(modifier = Modifier.size(50.dp), contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(R.drawable.img_help_radar),
                                contentDescription = null,
                                modifier = Modifier.size(50.dp),
                            )
                        }
                    }
                    ChevronDown(Modifier.align(Alignment.CenterHorizontally))
                    HelpRow(
                        title = "별 획득하기",
                        subtitle = "핵심 역량 당 300점을 모두 채우면 별을 얻어요",
                    ) {
                        // Star 4 (44x44) in 50x50
                        Box(modifier = Modifier.size(50.dp), contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.ic_tier_star),
                                contentDescription = null,
                                tint = StarYellow,
                                modifier = Modifier.size(44.dp),
                            )
                        }
                    }
                    ChevronDown(Modifier.align(Alignment.CenterHorizontally))
                    HelpRow(
                        title = "티어 승급하기",
                        subtitle = "별 3개를 다 모으면 다음 티어로!",
                    ) {
                        // 챌린저(마스터) 뱃지 — 승급 지향 아이콘(현재 티어 아님). 50 박스 꽉 채움
                        Box(modifier = Modifier.size(50.dp), contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(R.drawable.img_tier_master_s),
                                contentDescription = null,
                                modifier = Modifier.size(50.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

// 티어 승급 안내 항목 행 (height 50, gap 8): 아이콘50 + 텍스트콜럼. 폭은 hug(부제 한 줄 안 잘림)
@Composable
private fun HelpRow(
    title: String,
    subtitle: String,
    leading: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.height(50.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        leading()
        Column {
            Text(
                text = title,
                style = TqType.BodyL.figma().copy(fontWeight = FontWeight.Medium), // 16/500
                color = Gray800,
            )
            Text(
                text = subtitle,
                style = TqType.BodyM.figma(), // 14/400
                color = Gray600,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

// 항목 사이 chevron-down (24, Purple/600). 가운데 정렬은 호출부에서 Modifier.align으로
// (fillMaxWidth를 쓰면 hug Column이 전폭으로 늘어나 블록이 왼쪽에 붙어버림)
@Composable
private fun ChevronDown(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(R.drawable.ic_chevron_down),
        contentDescription = null,
        tint = Primary600,
        modifier = modifier.size(24.dp),
    )
}

@Preview(name = "성장 리포트", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun GrowthReportPreview() {
    TalkQQuestTheme {
        ReportContent(
            growth = GrowthTierReport(
                tierName = "골드",
                tierStars = 2,
                nextStarsNeeded = 1,
                nextTierName = "플래티넘",
                competencies = listOf(
                    Competency(CompetencyAxis.KINDNESS, "친절한 태도", "친절한 태도", 300, 70),
                    Competency(CompetencyAxis.INITIATIVE, "대화 주도", "대화 주도", 200, 70),
                    Competency(CompetencyAxis.EMPATHY, "공감 표현", "공감 능력", 100, 70),
                    Competency(CompetencyAxis.QUESTION_LINK, "질문 연결성", "질문 연결성", 300, 70),
                ),
            ),
            onBack = {},
        )
    }
}
