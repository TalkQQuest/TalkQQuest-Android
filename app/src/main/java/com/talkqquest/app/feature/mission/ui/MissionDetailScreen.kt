package com.talkqquest.app.feature.mission.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.Error
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray1000
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.LocalDesignScale
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.Success
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.component.TqButton
import com.talkqquest.app.core.designsystem.component.TqButtonSize
import com.talkqquest.app.core.designsystem.component.rememberHapticTick
import com.talkqquest.app.core.designsystem.softShadow
import com.talkqquest.app.feature.mission.data.model.MissionDetail
import com.talkqquest.app.feature.mission.data.model.MissionListItem
import com.talkqquest.app.feature.mission.viewmodel.MissionDetailUiState
import com.talkqquest.app.feature.mission.viewmodel.MissionDetailViewModel
import kotlin.math.abs

// ── 미션 상세 (UI 1차 v2.css "미션 상세"/"미션 상세에서 북마크" 전사) ──
// 화면 = 2단 분리(state hoisting): (1) viewModel 연결용 / (2) 값만 받아 그리는 부분(Preview용). 홈 패턴 동일.

// CSS 그라데이션 rgba(248,247,255,.8) → rgba(168,159,244,.01) 재현용 지점 21개.
// 안드로이드는 투명도를 색에 먼저 곱해 보간해서 두 점만 주면 중간 보라가 죽고,
// 지점을 조금만 찍으면 경계가 계단처럼 보임 → 중간값을 20등분으로 깔아 부드럽게.
// 색·투명도 모두 CSS 직선 보간 그대로 (이전의 투명도 급감 곡선(pow 0.8)은 중하단이
// 피그마보다 최대 46% 진해져 제거 — 2026-07-20 실기기 대조).
private val detailGradientStops: Array<Pair<Float, Color>> = Array(21) { i ->
    val t = i / 20f
    t to Color(
        red = (248f + (168f - 248f) * t) / 255f,
        green = (247f + (159f - 247f) * t) / 255f,
        blue = (255f + (244f - 255f) * t) / 255f,
        alpha = 0.8f + (0.01f - 0.8f) * t,
    )
}

// 카드 좌우 여백. CSS(UI_Z Frame 427321814)는 left 19 / width 360이라 오른쪽이 14인 비대칭이지만,
// 그건 프레임이 밀린 것으로 보고 오른쪽을 왼쪽에 맞춘다(사용자 결정 2026-08-18).
// 393에서 카드 폭은 393-19x2 = 355가 된다.
private val CardHorizontalMargin = 19.dp

// 내용이 화면에 다 들어가도 스크롤이 살아 있게 하려고 맨 아래에 두는 여분 높이.
// "스크롤은 상시 있고 3줄일 때도 살짝 올라간다"는 지시(2026-08-18)를 위한 것 — CSS에는 없는 값이다.
private val ScrollSlack = 24.dp

// 제목 줄바꿈 계산 결과. text = 그릴 문자열(2줄이면 \n 포함), lineCount = 1 또는 2
// (중앙 블록 높이 계산에 그대로 쓰인다), ellipsize = 최소 줄 수가 3 이상이라 2줄로 강제하고
// 둘째 줄을 말줄임했는지.
private data class MissionTitleLayout(val text: String, val lineCount: Int, val ellipsize: Boolean)

// 홈 미션 제목과 동일한 줄바꿈: 한 줄이면 유지하고, 넘치면 필요한 최소 줄 수를 구한 뒤
// 어절 경계에서 각 줄의 실제 폭이 최대한 균등해지도록 나눈다. 다만 최소 줄 수가 3 이상이면
// 2줄로 강제한다 — 첫 줄은 최대 폭을 채우는 그리디로 채우고 둘째 줄만 말줄임한다.
// rememberTextMeasurer로 측정만 하고 상태에는 쓰지 않는 순수 계산이라 되먹임이 없다.
@Composable
private fun rememberMissionTitleLayout(title: String, maxWidth: Dp): MissionTitleLayout {
    val style = TqType.HeadingL.figma()
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    return remember(title, maxWidth) {
        val maxWidthPx = with(density) { maxWidth.toPx() }
        fun width(text: String): Int = measurer.measure(AnnotatedString(text), style = style).size.width
        val rawWords = title.trim().split(Regex("\\s+")).filter(String::isNotEmpty)
        val words = buildList {
            var index = 0
            while (index < rawWords.size) {
                if (rawWords[index].length == 1 && index + 1 < rawWords.size) {
                    add("${rawWords[index]} ${rawWords[index + 1]}")
                    index += 2
                } else {
                    add(rawWords[index])
                    index++
                }
            }
        }
        val oneLine = words.joinToString(" ")
        if (width(oneLine) <= maxWidthPx || words.size <= 1) {
            return@remember MissionTitleLayout(oneLine, lineCount = 1, ellipsize = false)
        }
        val minimumLines = buildList {
            var current = ""
            words.forEach { word ->
                val candidate = if (current.isEmpty()) word else "$current $word"
                if (current.isNotEmpty() && width(candidate) > maxWidthPx) {
                    add(current)
                    current = word
                } else current = candidate
            }
            if (current.isNotEmpty()) add(current)
        }
        val minimumLineCount = minimumLines.size
        if (minimumLineCount >= 3) {
            // 최소 줄 수가 3 이상이면 2줄로 강제. 첫 줄은 그리디로 최대 폭을 채우고,
            // 나머지는 전부 둘째 줄로 보내 Text의 maxLines=2 + ellipsis가 잘라내게 한다.
            var firstLine = ""
            var splitIndex = 0
            while (splitIndex < words.size) {
                val candidate = if (firstLine.isEmpty()) words[splitIndex] else "$firstLine ${words[splitIndex]}"
                if (firstLine.isNotEmpty() && width(candidate) > maxWidthPx) break
                firstLine = candidate
                splitIndex++
            }
            val secondLine = words.subList(splitIndex, words.size).joinToString(" ")
            return@remember MissionTitleLayout("$firstLine\n$secondLine", lineCount = 2, ellipsize = true)
        }
        val targetWidth = minimumLines.sumOf { width(it) }.toFloat() / minimumLineCount
        data class Partition(val lines: List<String>, val score: Float)
        val memo = mutableMapOf<Pair<Int, Int>, Partition?>()
        fun choose(start: Int, linesLeft: Int): Partition? {
            if (start >= words.size) return null
            if (linesLeft == 1) {
                val last = words.subList(start, words.size).joinToString(" ")
                val lastWidth = width(last)
                return if (lastWidth <= maxWidthPx || start == words.lastIndex) {
                    Partition(listOf(last), abs(lastWidth - targetWidth))
                } else null
            }
            val key = start to linesLeft
            memo[key]?.let { return it }
            var current = ""
            var best: Partition? = null
            for (end in start until words.size - (linesLeft - 1)) {
                current = if (current.isEmpty()) words[end] else "$current ${words[end]}"
                val currentWidth = width(current)
                if (currentWidth > maxWidthPx && end > start) break
                val tail = choose(end + 1, linesLeft - 1) ?: continue
                val candidate = Partition(
                    lines = listOf(current) + tail.lines,
                    score = abs(currentWidth - targetWidth) + tail.score,
                )
                if (best == null || candidate.score < best!!.score) best = candidate
            }
            memo[key] = best
            return best
        }
        val text = choose(0, minimumLineCount)?.lines?.joinToString("\n") ?: minimumLines.joinToString("\n")
        MissionTitleLayout(text, lineCount = minimumLineCount, ellipsize = false)
    }
}

@Composable
private fun MissionDetailTitleText(layout: MissionTitleLayout, maxWidth: Dp) {
    Text(
        text = layout.text,
        style = TqType.HeadingL.figma(),
        color = Gray700,
        textAlign = TextAlign.Center,
        modifier = Modifier.widthIn(max = maxWidth),
        maxLines = if (layout.ellipsize) 2 else Int.MAX_VALUE,
        overflow = if (layout.ellipsize) TextOverflow.Ellipsis else TextOverflow.Clip,
    )
}

@Composable
fun MissionDetailScreen(
    viewModel: MissionDetailViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onNextClick: (String) -> Unit = {},
    onMissionClick: (String) -> Unit = {}, // 시트 안 카드 클릭 → 그 미션 상세
    onSheetTopChange: (Float?) -> Unit = {},
    onSavedListClick: () -> Unit = {}, // 시트 "저장 목록 >" → 저장 목록 화면
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // 다른 화면(저장 목록 등)에서 바꾼 북마크가 돌아왔을 때 반영되도록, 복귀마다 조용히 재조회
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.loadDetail(showLoading = false) }
    MissionDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::loadDetail,
        onToggleSave = viewModel::toggleSave,
        onToggleSaveInList = viewModel::toggleSaveInList,
        onDismissSaveSheet = viewModel::dismissSaveSheet,
        onNextClick = onNextClick,
        onMissionClick = onMissionClick,
        onSheetTopChange = onSheetTopChange,
        onSavedListClick = onSavedListClick,
    )
}

@Composable
private fun MissionDetailScreen(
    uiState: MissionDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onToggleSave: () -> Unit,
    onToggleSaveInList: (String) -> Unit = {},
    onDismissSaveSheet: () -> Unit = {},
    onNextClick: (String) -> Unit = {},
    onMissionClick: (String) -> Unit = {},
    onSheetTopChange: (Float?) -> Unit = {},
    onSavedListClick: () -> Unit = {},
) = FitDesign { // 작은 화면에선 디자인(393x852) 통째 축소 — 스크롤 없이 한 화면에
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

            uiState.detail != null -> {
                // 북마크 저장 시트 (미션 목록과 같은 부품 재사용)
                MissionSaveSheetScaffold(
                    savedMission = uiState.saveSheetMission,
                    recentSavedMissions = uiState.otherSavedMissions,
                    onDismiss = onDismissSaveSheet,
                    onMissionClick = { id ->
                        onDismissSaveSheet()
                        onMissionClick(id)
                    },
                    // 방금 저장한(현재) 미션은 상세 상태를, 저장 목록의 다른 미션은 목록 상태를 토글.
                    onToggleSave = { id ->
                        if (id == uiState.detail.id) onToggleSave() else onToggleSaveInList(id)
                    },
                    onSheetTopChange = onSheetTopChange,
                    // 시트에서 "저장 목록 >" → 시트 닫고 저장 목록 화면으로
                    onSavedListClick = {
                        onDismissSaveSheet()
                        onSavedListClick()
                    },
                ) {
                    MissionDetailContent(
                        detail = uiState.detail,
                        onBack = onBack,
                        onToggleSave = onToggleSave,
                        onNextClick = onNextClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun MissionDetailContent(
    detail: MissionDetail,
    onBack: () -> Unit,
    onToggleSave: () -> Unit,
    onNextClick: (String) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // 세로 배치: 일러스트·여백은 FitDesign이 이미 화면 비율만큼 균일 축소하므로 추가 축소 안 함(shrink=1).
        //  (designScale을 shrink로 또 곱하면 이중 축소돼 다트가 과하게 작아짐 — 그 실수 되돌림)
        // compact(극소형, 더 줄일 여지 없음)는 그라데이션 높이만 CSS 축소 비율로 처리하고,
        // 나머지 배치는 화면 크기와 무관하게 항상 같은 계산식을 쓴다.
        val shrink = 1f
        val compact = LocalDesignScale.current <= 0.5f

        val density = LocalDensity.current
        val tick = rememberHapticTick()

        // 카드 폭 = 화면 폭 - 좌우 여백(19씩). 393에서 355. 제목도 같은 폭을 쓴다 —
        // 제목이 카드보다 넓어지지 않게 하라는 지시(2026-08-18)라 두 값을 한 곳에서 뽑는다.
        val contentWidth = maxWidth - CardHorizontalMargin * 2

        // 제목 줄바꿈은 여기서 한 번만 계산해 그리기(MissionDetailTitleText)와 아래 그라데이션
        // 높이 계산에 동일하게 쓴다. 측정만 하고 상태에 쓰지 않으므로 되먹임이 없다.
        val titleLayout = rememberMissionTitleLayout(detail.title, contentWidth)

        // 중앙 블록 → 카드 간격: 설명이 몇 줄이든 항상 CSS 값(33) 그대로 둔다. 카드가 늘어난
        // 만큼은 아래로 그대로 밀려나 스크롤로 처리한다(줄 수에 따른 보정 없음).
        val cardsTopGap = 33.dp

        // 상단 보라 그라데이션 배경 (CSS Frame 436: 높이 462). 카드 머리 위치를 실측하지 않고
        // 계산으로 구한다 — 헤더·중앙 블록·여백이 전부 고정값이거나 위에서 이미 구한 값이라
        // 카드 Column을 그리기 전에도 알 수 있다. 그라데이션 끝은 카드 머리보다 37 아래(CSS
        // 462-425)로, 카드 머리를 살짝 덮는다.
        val statusBarInset = with(density) { WindowInsets.statusBars.getTop(this).toDp() }
        val centerBlockHeight = 137.dp + 2.dp + (34 * titleLayout.lineCount).dp + 16.dp + 30.dp
        val cardsTop = statusBarInset + 8.dp + 44.dp + 47.dp + centerBlockHeight + cardsTopGap
        val gradientHeight = cardsTop + 37.dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (compact) 462.dp * shrink else gradientHeight)
                .background(Brush.verticalGradient(*detailGradientStops)),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            // 스크롤되는 건 내용(헤더~카드)까지다. 아래 버튼 행은 이 Column에 고정으로 남아
            // 내용이 아무리 길어져도 항상 같은 자리에 보인다(사용자 지시 2026-08-18).
            // 위치는 기억하지 않아 다른 화면 갔다 오면 매번 맨 위.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(remember { ScrollState(0) }),
            ) {
            Spacer(Modifier.height(8.dp)) // 상태바 → 헤더 (CSS Frame 361 top 48 = 상태바 40 + 8)
            // 헤더 (CSS Frame 427321190): 뒤로가기 44 왼끝, 제목은 화면 가로 정중앙.
            // (제목 x166~226 → 중심 196 = 화면 중심 393/2. 개정 전 "화살표 옆" 배치에서 변경됨)
            Box(modifier = Modifier.fillMaxWidth().height(44.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape) // 눌림 효과를 네모 대신 동그라미로 (아이콘 버튼 관례)
                        .clickable(onClick = { tick(); onBack() }),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back_chevron),
                        contentDescription = "뒤로가기",
                        tint = Gray500, // 기본 프레임 기준 (변형 프레임은 Gray800 — 목록과 통일해 Gray500, 합의됨)
                    )
                }
                Text(
                    text = "미션 상세",
                    style = TqType.BodyL.figma(),
                    color = Gray800,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            // 헤더(92) → 중앙 블록(top 139) = CSS 47. 스크롤 컬럼 안에서는 weight를 쓸 수 없어 고정값.
            Spacer(Modifier.height(47.dp * shrink))

            // 중앙 블록 (CSS Frame 435): 다트 과녁 → 제목 → 칩 3개, 모두 가운데 정렬
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 다트 과녁 일러스트 — 위치·크기 모두 고정(축소도, 위로 올리기도 없음).
                // (CSS: 노드 117x123.68 + -7도 회전 → 회전 후 바깥 경계 131x137)
                // PNG(876x928) = 노드 틀(117x123.68)의 7.5배 캔버스에 회전만 구운 것 — 기준 틀은 회전 전 노드.
                // → 117x123.68로 그려야 피그마와 같은 배율. 131x137로 그리면 10.5% 확대됨
                //   (2026-07-20 확정: "다트 커 보임" 제보 → 배율 재계산. 이전 "1% 일치" 결론은 피그마 쪽에
                //    회전 확장을 이중 적용한 오계산이었음). 레이아웃 자리는 CSS 틀 131x137을 박스로 유지.
                Box(
                    modifier = Modifier.size(width = 131.dp * shrink, height = 137.dp * shrink),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_mission_target),
                        contentDescription = null,
                        modifier = Modifier.size(width = 117.dp * shrink, height = 123.68.dp * shrink),
                    )
                }
                // 이미지 → 제목: CSS Frame 435 gap 2. PNG(876x928) 알파 실측 결과 투명 여백이
                // 피그마 틀과 같은 비율로 그대로 있음(상8.5%/하4.7%) → 틀 기준이 동일하므로 CSS 값 2를 그대로 쓴다.
                // (구 주석 "PNG는 잘라냄→눈대조 16"은 오판이었음 — 16이면 제목·칩이 6px 내려감, 2026-07-19 전수검증)
                Spacer(Modifier.height(2.dp * shrink))
                // 홈 미션 제목과 같은 최소 줄 수·어절 경계·줄 폭 균형 규칙.
                MissionDetailTitleText(layout = titleLayout, maxWidth = contentWidth)
                Spacer(Modifier.height(16.dp)) // 제목 → 칩 (CSS Frame 434 gap)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { // 칩 간격 (CSS Frame 349 gap)
                    DetailDifficultyChip(difficulty = detail.difficulty)
                    DetailInfoChip(text = detail.category)
                    DetailInfoChip(text = "${detail.estimatedMinutes}분")
                }
            }

            // 중앙 블록 → 카드(top 425) = CSS 33. 설명 줄 수와 무관하게 항상 고정.
            Spacer(Modifier.height(cardsTopGap * shrink))

            // 설명·체크·보상 카드 (CSS Frame 427321814: 카드 간격 16). 좌우 여백은 CardHorizontalMargin.
            Column(
                modifier = Modifier.padding(horizontal = CardHorizontalMargin),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                DescriptionCard(description = detail.description)
                BenefitsCard(benefits = detail.benefits)
                RewardCard(rewardXp = detail.rewardXp)
            }

            // 카드 → 하단 액션 = CSS 24, 줄 수와 무관하게 항상 고정.
            Spacer(Modifier.height(24.dp))
            // 스크롤은 항상 살아 있어야 한다(사용자 지시) — 3줄짜리 짧은 미션도 살짝 위로 올릴 수 있게
            // 내용을 이만큼 더 길게 만든다. 맨 위로 올려두면 위 배치는 CSS 그대로다.
            Spacer(Modifier.height(ScrollSlack))
            } // 스크롤되는 내용 Column

            // 하단 액션 (CSS Frame 366): 북마크 + "다음" 버튼 — 스크롤 밖 고정
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // CSS Frame 366은 padding 0 24 + gap 12 + 버튼 295 고정인데 합이 399라 393을 6 넘침.
                    // 피그마 실렌더는 좌 24에서 시작해 375에서 끝나므로 실제 우측 여백은 18.
                    // 좌24/우18로 두면 393에서 버튼이 정확히 295가 되고(피그마 일치), 다른 폭에선 버튼이 늘고 준다.
                    .padding(start = 24.dp, end = 18.dp), // 하단 여백은 아래 알약 스페이서가 처리
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp), // 북마크 ↔ 버튼 (CSS gap)
            ) {
                // 북마크: 리플 없이 아이콘 색만 변화 (목록과 동일 규칙)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onToggleSave,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(
                            if (detail.isSaved) R.drawable.ic_mission_bookmark_filled else R.drawable.ic_mission_bookmark,
                        ),
                        contentDescription = if (detail.isSaved) "북마크 해제" else "북마크",
                    )
                }
                TqButton(
                    text = "다음",
                    onClick = { onNextClick(detail.id) },
                    modifier = Modifier.weight(1f), // 393에선 정확히 295(피그마 일치), 그 외 폭에선 유동
                    size = TqButtonSize.Large, // 52 / radius 16 = CSS 버튼L과 동일
                )
            }

            // 버튼 아래 여백. 이 화면엔 앱 하단 알약 네비가 없다 — CSS "미션 상세" 프레임에도
            // 홈에 있는 `하단 네비게이션` 프레임이 없고 시스템 네비(top 804)만 있다.
            // 그래서 알약 자리 92를 비워두지 않고, CSS대로 버튼 바닥 780 → 시스템 네비 804의 24만 둔다.
            Spacer(Modifier.height(24.dp))
        }
    }
}

// 난이도 칩 (CSS Frame 345): padding 4x12, radius 16, Label/L. 색은 미션 목록 알약과 동일 값.
@Composable
private fun DetailDifficultyChip(difficulty: String) {
    val (textColor, bgColor) = when (difficulty) {
        "쉬움" -> Success to EasyBg
        "보통" -> OrangeText to NormalBg
        "어려움" -> Error to HardBg
        else -> Gray700 to Gray50
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(text = difficulty, style = TqType.LabelL.figma(), color = textColor)
    }
}

// 카테고리·시간 칩 (CSS Frame 342/346): padding 4x12, radius 16, 배경 Gray50, Body/M Gray500.
// 배경이 라벤더 그라데이션 위라 Gray50 알약이 도드라져 보임 (CSS 값 그대로).
@Composable
private fun DetailInfoChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Gray50)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(text = text, style = TqType.BodyM.figma(), color = Gray500)
    }
}

// 설명 카드 (UI_Z.css "미션 상세" Frame 447: 흰 배경 r8 카드그림자, padding 12x16, gap 6).
// 체크 표시 없이 미션 설명 전문을 한 문단으로 보여준다 — 홈에서 두 줄로 잘린 뒷부분을
// 여기서 다 볼 수 있게 하는 것이 목적(2026-08-18). 세 카드 중 맨 위, 모서리만 8로 다르다.
// 글자 스타일: Body/M(14sp), Gray/800(#273449).
@Composable
private fun DescriptionCard(description: String) {
    Text(
        text = description,
        style = TqType.BodyM.figma(),
        color = Gray800,
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

// 효과 카드 (UI_Z.css "미션 상세" Frame 446: 흰 배경 r20 카드그림자, padding 12x16, 줄 간격 6).
// 줄 = 체크(24, 보라) + 문구. 개수는 서버 가변(디자인은 2개 예시).
// 문구 스타일은 CSS 그대로 Body/M(14sp), Gray/600(#475569) — 옛 코드의 Body/L·Gray/900이 아니다.
@Composable
private fun BenefitsCard(benefits: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        benefits.forEach { benefit ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp), // 체크 ↔ 문구 (CSS gap)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_benefit_check), // CSS check-01 수치 그대로(글리프 9.6x7.2+stroke2)
                    contentDescription = null,
                    tint = Primary600,
                    modifier = Modifier.size(24.dp),
                )
                Text(text = benefit, style = TqType.BodyM.figma(), color = Gray600)
            }
        }
    }
}

// 보상 카드 (CSS Frame 440): 흰 배경 r20 카드그림자, padding 12x16, 선물상자(55) + 문구.
@Composable
private fun RewardCard(rewardXp: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(15.dp), // 이미지 ↔ 문구 (CSS gap)
    ) {
        // PNG(165x165) = 피그마 55x55 노드의 정확한 3x export(여백 포함) → 55로 그리면 1:1.
        // (구 에셋 134x144는 이 export에서 투명 여백이 잘린 버전이라 선물이 ~20% 크게 보였음 — 2026-07-20 교체)
        Image(
            painter = painterResource(R.drawable.img_mission_gift),
            contentDescription = null,
            modifier = Modifier.size(55.dp),
        )
        Column {
            Text(
                text = "미션 보상",
                style = TqType.BodyM.figma(),
                color = Gray700,
                modifier = Modifier.padding(start = 2.dp), // CSS Frame 439 padding 0 2 — 라벨만 2 들여쓰기
            )
            Text(text = "+${rewardXp} XP", style = TqType.TitleL.figma(), color = Primary600)
        }
    }
}

// ── Preview ──
private val previewDetail = MissionDetail(
    id = "1",
    title = "처음 보는 사람에게 짧게 인사하기",
    category = "짧은 대화",
    difficulty = "쉬움",
    estimatedMinutes = 2,
    rewardXp = 20,
    description = "낯선 사람과의 첫 대화에 자신감이 생기고, 자연스럽게 대화를 이어갈 수 있어요.",
    benefits = listOf(
        "낯선 사람과의 첫 대화에 자신감이 생겨요",
        "자연스럽게 대화를 이어갈 수 있어요",
    ),
)

@Preview(name = "미션 상세 (393dp 실기기)", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun MissionDetailScreenPreview() {
    TalkQQuestTheme {
        MissionDetailScreen(
            uiState = MissionDetailUiState(detail = previewDetail),
            onBack = {}, onRetry = {}, onToggleSave = {},
        )
    }
}

// 긴 제목(3줄 이상 → 2줄+말줄임) + 긴 설명(카드가 길어진 만큼 그대로 아래로 밀려나
// 스크롤됨): 다트·버튼 위치는 고정인 채로 카드만 늘어나는지 확인.
@Preview(name = "미션 상세 - 긴 제목·긴 설명", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun MissionDetailScreenLongPreview() {
    TalkQQuestTheme {
        MissionDetailScreen(
            uiState = MissionDetailUiState(
                detail = previewDetail.copy(
                    title = "동아리에서 관심사가 비슷한 사람에게 먼저 말 걸어보기",
                    difficulty = "어려움",
                    estimatedMinutes = 15,
                    rewardXp = 60,
                    description = "일상 속 이야깃거리를 찾는 눈이 생기고, 상대방과 공감대를 만들 수 있고, " +
                        "대화를 오래 이어가는 힘이 생겨요. 짧은 인사를 넘어서 관심사가 비슷한 사람과 " +
                        "자연스럽게 대화를 시작하고, 편안한 분위기 속에서 서로에 대해 조금씩 알아가면서 " +
                        "다음 만남을 기대하게 되는 경험을 쌓을 수 있어요.",
                ),
            ),
            onBack = {}, onRetry = {}, onToggleSave = {},
        )
    }
}

// 좁은 화면(320dp): 스크롤·버튼 겹침 확인.
@Preview(name = "미션 상세 (320dp 좁은 화면)", showSystemUi = true, device = "spec:width=320dp,height=640dp")
@Composable
private fun MissionDetailScreenNarrowPreview() {
    TalkQQuestTheme {
        MissionDetailScreen(
            uiState = MissionDetailUiState(detail = previewDetail),
            onBack = {}, onRetry = {}, onToggleSave = {},
        )
    }
}

@Preview(name = "미션 상세 - 에러", showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun MissionDetailScreenErrorPreview() {
    TalkQQuestTheme {
        MissionDetailScreen(
            uiState = MissionDetailUiState(errorMessage = "네트워크 연결을 확인해주세요."),
            onBack = {}, onRetry = {}, onToggleSave = {},
        )
    }
}
