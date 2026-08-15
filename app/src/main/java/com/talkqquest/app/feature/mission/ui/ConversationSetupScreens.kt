package com.talkqquest.app.feature.mission.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.animateIntSizeAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.talkqquest.app.R
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray200
import com.talkqquest.app.core.designsystem.Gray100
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Primary50
import com.talkqquest.app.core.designsystem.Primary100
import com.talkqquest.app.core.designsystem.Primary200
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.Primary700
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.component.TqButton
import com.talkqquest.app.core.designsystem.component.rememberHapticTick
import com.talkqquest.app.feature.mission.data.model.MissionSetupEnvironment
import com.talkqquest.app.feature.mission.data.model.MissionSetupPartnerAgeGroup
import com.talkqquest.app.feature.mission.data.model.MissionSetupPartnerGender
import com.talkqquest.app.feature.mission.data.model.MissionSetupPartnerRole
import com.talkqquest.app.feature.mission.data.model.SetupAgeGroupByIndex
import com.talkqquest.app.feature.mission.data.model.SetupEnvironmentByIndex
import com.talkqquest.app.feature.mission.data.model.SetupGenderByIndex
import com.talkqquest.app.feature.mission.data.model.SetupPartnerRoleByIndex

// ── 미션 진입 · 대화 설정 4스텝 (UI 13차 "진입- 대화 설정 1~4" 전사) ──
// 미션 상세 "다음" → 1(장소)→2(상대)→3(성별·나이)→4(친밀도·말투) → 대화. 옛 ConversationPrepScreen 대체.
//
// ★옵션 라벨·아이콘·세부설명은 피그마 실렌더(스크린샷) 기준. CSS 레이어명은 플레이스홀더라 못 씀.
// ★아이콘 = 디자이너 SVG를 vector drawable로 변환(ic_setup_*).
// ★선택값은 네 화면이 공유하는 ConversationSetupViewModel에 모았다가 4단계 "대화 시작하기"에서
//   POST /missions/{id}/setups로 보낸다(2026-08-13 연동). 각 화면은 카드 index를 서버 enum으로
//   바꿔 넘기기만 하고(SetupXxxByIndex), 화면 안의 선택 표시는 예전처럼 로컬 상태가 그린다.
//   ※카드 순서를 바꾸면 MissionSetupModels.kt의 SetupXxxByIndex도 같이 바꿔야 한다.
// ★선택 상태 색은 디자이너 컴포넌트 CSS(component.css)의 default↔filled 변형 그대로.
//   화면 CSS엔 미선택만 깔려 있어 예전엔 앱 관례로 임시 지정했었는데, 컴포넌트가 와서 전부 교체했다.

private data class PlaceOption(val label: String, val subtitle: String?, val icon: Int)
private data class IconOption(val label: String, val icon: Int)

private val placeOptions = listOf(
    PlaceOption("학교/ 대학교", null, R.drawable.ic_setup_place_school),
    PlaceOption("직장", null, R.drawable.ic_setup_place_work),
    PlaceOption("일상공간", "카페, 헬스장, 편의점 등", R.drawable.ic_setup_place_daily),
    PlaceOption("모임", "동아리, 스터디, 소모임 등", R.drawable.ic_setup_place_gathering),
    PlaceOption("온라인", "SNS, 채팅", R.drawable.ic_setup_place_online),
)
private val partnerOptions = listOf(
    IconOption("친구", R.drawable.ic_setup_partner_friend),
    IconOption("동기/ 동료", R.drawable.ic_setup_partner_colleague),
    IconOption("선배", R.drawable.ic_setup_partner_senior),
    IconOption("후배", R.drawable.ic_setup_partner_junior),
    IconOption("직접 설정", R.drawable.ic_setup_partner_custom),
)
private val genderOptions = listOf("남성", "여성")
private val ageOptions = listOf("10대", "20대", "30대", "40대", "50대", "60대 이상")

// 선택된 점의 보라 글로우 (CSS: 0 0 12 rgba(114,100,248,0.6))
private val GlowPurple = Color(0xFF7264F8)

// ══════════════════ 공용 스캐폴드 ══════════════════

@Composable
private fun SetupStepScaffold(
    step: Int,
    onBack: () -> Unit,
    nextEnabled: Boolean,
    onNext: () -> Unit,
    nextText: String = "다음",
    content: @Composable ColumnScope.() -> Unit,
) = FitDesign {
    val tick = rememberHapticTick()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            Spacer(Modifier.height(8.dp)) // 상태바 → 상단바 (CSS Frame 427321663 top 48)
            // 상단바: 뒤로가기(좌) · N / 4(우) — CSS Frame 427321663 left 0 width 377 → 왼끝(x=0), 오른쪽만 16 인셋
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(onClick = { tick(); onBack() }),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back_chevron),
                        contentDescription = "뒤로가기",
                        tint = Gray500,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { // CSS Frame 427321662 gap 6
                    Text("$step", style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(), color = Gray700)
                    Text("/ 4", style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(), color = Gray400)
                }
            }

            Spacer(Modifier.height(30.dp)) // 상단바 끝(92) → 헤더(top 122)
            // 콘텐츠(헤더·카드)는 좌우 16 인셋 (상단바만 x=0)
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                content()
            }
        }

        // 하단 버튼 (CSS 버튼L: 361x52 / Primary600 radius 16 / 아래 72 = 네비+24)
        TqButton(
            text = nextText,
            onClick = onNext,
            enabled = nextEnabled,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
                .fillMaxWidth(),
        )
    }
}

// 헤더: 제목(Heading/L Gray800) + 부제(Body/L Gray500), gap 2. CSS Frame 427321656 (left pad 7).
@Composable
private fun SetupHeader(title: String, subtitle: String) {
    Column(
        modifier = Modifier.padding(start = 7.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(text = title, style = TqType.HeadingL.figma(), color = Gray800)
        Text(text = subtitle, style = TqType.BodyL.figma(), color = Gray500)
    }
}

// 선택 상태 색 — 디자이너 컴포넌트 CSS(component.css)의 default ↔ filled 쌍 그대로.
// 화면 CSS(ui_14)엔 미선택 상태만 깔려 있어 눌렀을 때 색은 이 컴포넌트 파일이 유일한 기준이다.
//  · 장소·상대 카드: 배경 Purple/100 + 테두리 Purple/600 + 아이콘 원 Purple/200 + 아이콘/제목 Purple/600·700
//  · 성별·나이 칩  : 배경 Purple/600 + 테두리 제거 + Body/L Medium + 글자 Purple/50 (칩만 반전형)
// 테두리 굵기는 두 상태 다 1px (예전엔 선택 시 1.5로 굵혔는데 CSS에 없던 임의값이라 되돌림).
private fun cardBorderColor(selected: Boolean) = if (selected) Primary600 else Gray200
private fun cardBg(selected: Boolean, unselected: Color) = if (selected) Primary100 else unselected
private fun cardIconTint(selected: Boolean, unselected: Color) = if (selected) Primary600 else unselected
private fun cardLabelColor(selected: Boolean) = if (selected) Primary700 else Gray800

// ══════════════════ 화면 1 · 장소 (세로 리스트) ══════════════════
// 카드: 아이콘(40 원형) + 제목 + 선택적 세부설명. CSS 361x76, radius 16, gap 12.

@Composable
private fun PlaceCard(
    option: PlaceOption,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconCircleColor by animateColorAsState(
        targetValue = when {
            !enabled -> Gray200
            selected -> Primary200
            else -> Gray200
        },
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "placeCardIconCircleColor",
    )
    val iconColor by animateColorAsState(
        targetValue = if (!enabled) Gray300 else cardIconTint(selected, Gray400),
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "placeCardIconColor",
    )
    val labelColor by animateColorAsState(
        targetValue = if (!enabled) Gray400 else cardLabelColor(selected),
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "placeCardLabelColor",
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Transparent)
            .border(1.dp, Color.Transparent, RoundedCornerShape(16.dp))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                // CSS Frame 427321657 배경 = 미선택 Gray/200(#E2E8F0) / 선택 Purple/200(#E4DFFF)
                .background(iconCircleColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(option.icon),
                contentDescription = null,
                // 화면 CSS(ui_14) Vector 미선택 #94A3B8(Gray/400) / 선택 Purple/600.
                // 컴포넌트 CSS엔 Gray/500으로 남아 있지만 화면 인스턴스 5개가 전부 Gray/400이고
                // 상대 카드 아이콘도 Gray/400이라 화면 쪽(=최종 기준)으로 맞춘다.
                tint = iconColor,
                // 크기 미지정 = 벡터 native(학교 27×20 등) 그대로 = CSS Vector 크기와 일치.
                // 정사각 size()로 우기면 비정사각 글리프가 비율맞춤으로 작아짐(작게 보이던 원인)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = option.label, style = TqType.TitleL.figma(), color = labelColor)
            if (option.subtitle != null) {
                // 세부 설명 = Body/M Gray/700(#334155). 선택돼도 색이 안 바뀐다(filled 변형도 Gray/700 그대로)
                Text(
                    text = option.subtitle,
                    style = TqType.BodyM.figma(),
                    color = if (enabled) Gray700 else Gray400,
                )
            }
        }
    }
}

@Composable
fun ConversationSetup1Screen(
    onBack: () -> Unit = {},
    onNext: () -> Unit = {},
    // 고른 장소를 서버 enum으로 바꿔 넘긴다. 카드 목록이 여기 있으니 매핑도 여기서 한다.
    onSelect: (MissionSetupEnvironment) -> Unit = {},
    initialSelection: MissionSetupEnvironment? = null,
    disabledOptions: Set<MissionSetupEnvironment> = emptySet(),
    isLoading: Boolean = false,
) {
    if (isLoading) {
        FitDesign {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Gray50),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Primary600)
            }
        }
        return
    }
    var selected by remember(initialSelection) {
        mutableStateOf(initialSelection?.let(SetupEnvironmentByIndex::indexOf)?.takeIf { it >= 0 })
    }
    var hasMovedSelection by remember { mutableStateOf(false) }
    SetupStepScaffold(step = 1, onBack = onBack, nextEnabled = selected != null, onNext = onNext) {
        SetupHeader("어디에서 나눌 대화인가요?", "대화 장소를 선택해주세요.")
        Spacer(Modifier.height(24.dp))
        var bounds by remember { mutableStateOf<Map<Int, Pair<IntOffset, IntSize>>>(emptyMap()) }
        var listOffset by remember { mutableStateOf(IntOffset.Zero) }
        val selectedBounds = selected?.let { bounds[it] }
        val density = LocalDensity.current
        val animatedOffset by animateIntOffsetAsState(
            targetValue = selectedBounds?.first ?: IntOffset.Zero,
            animationSpec = if (hasMovedSelection) {
                tween(240, easing = FastOutSlowInEasing)
            } else {
                snap()
            },
            label = "setup1SelectionOffset",
        )
        val animatedSize by animateIntSizeAsState(
            targetValue = selectedBounds?.second ?: IntSize.Zero,
            animationSpec = if (hasMovedSelection) {
                tween(240, easing = FastOutSlowInEasing)
            } else {
                snap()
            },
            label = "setup1SelectionSize",
        )
        val selectionAlpha by animateFloatAsState(
            targetValue = if (selected != null) 1f else 0f,
            animationSpec = tween(220, easing = FastOutSlowInEasing),
            label = "setup1SelectionAlpha",
        )
        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            bounds.forEach { (index, measured) ->
                val (offset, size) = measured
                val disabled = SetupEnvironmentByIndex[index] in disabledOptions
                Box(
                    modifier = Modifier
                        .offset { offset }
                        .size(
                            with(density) { size.width.toDp() },
                            with(density) { size.height.toDp() },
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (disabled) Gray100 else Gray50)
                        .border(1.dp, Gray200, RoundedCornerShape(16.dp)),
                )
            }
            if (animatedSize != IntSize.Zero) {
                Box(
                    modifier = Modifier
                        .offset { animatedOffset }
                        .size(
                            with(density) { animatedSize.width.toDp() },
                            with(density) { animatedSize.height.toDp() },
                        )
                        .graphicsLayer { alpha = selectionAlpha }
                        .clip(RoundedCornerShape(16.dp))
                        .background(Primary100)
                        .border(1.dp, Primary600, RoundedCornerShape(16.dp)),
                )
            }
            Column(
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    val position = coordinates.positionInParent()
                    listOffset = IntOffset(position.x.roundToInt(), position.y.roundToInt())
                },
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                placeOptions.forEachIndexed { i, opt ->
                    val enabled = SetupEnvironmentByIndex[i] !in disabledOptions
                    PlaceCard(
                        option = opt,
                        selected = selected == i,
                        enabled = enabled,
                        onClick = {
                            if (selected != null && selected != i) hasMovedSelection = true
                            selected = i
                            onSelect(SetupEnvironmentByIndex[i])
                        },
                        modifier = Modifier.onGloballyPositioned { coordinates ->
                            val position = coordinates.positionInParent()
                            bounds = bounds + (
                                i to (listOffset + IntOffset(position.x.roundToInt(), position.y.roundToInt()) to coordinates.size)
                            )
                        },
                    )
                }
            }
        }
    }
}

// ══════════════════ 화면 2 · 상대 (2열 그리드) ══════════════════
// 카드: 173x90, radius 16, 아이콘(30) 위 + 라벨 아래. 행 gap 17 · 열 gap 15.

@Composable
private fun PartnerCard(
    option: IconOption,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconColor by animateColorAsState(
        targetValue = if (!enabled) Gray300 else cardIconTint(selected, Gray400),
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "partnerCardIconColor",
    )
    val labelColor by animateColorAsState(
        targetValue = if (!enabled) Gray400 else cardLabelColor(selected),
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "partnerCardLabelColor",
    )
    Column(
        modifier = modifier
            .height(90.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Transparent)
            .border(1.dp, Color.Transparent, RoundedCornerShape(16.dp))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(option.icon),
            contentDescription = null,
            tint = iconColor, // CSS Vector 미선택 #94A3B8(Gray/400) / 선택 Purple/600
            // 크기 미지정 = 벡터 native(친구 25×19 등) = CSS Vector(30 슬롯 안 글리프). 정사각 size(30)이면 비율 왜곡
        )
        Spacer(Modifier.height(4.dp)) // CSS Frame 427321664 gap 4
        // CSS 상대 라벨 = 18/600 (Title/L)
        Text(text = option.label, style = TqType.TitleL.figma(), color = labelColor)
    }
}

@Composable
fun ConversationSetup2Screen(
    onBack: () -> Unit = {},
    onNext: () -> Unit = {},
    onSelect: (MissionSetupPartnerRole) -> Unit = {},
    initialSelection: MissionSetupPartnerRole? = null,
    disabledOptions: Set<MissionSetupPartnerRole> = emptySet(),
) {
    var selected by remember(initialSelection) {
        mutableStateOf(initialSelection?.let(SetupPartnerRoleByIndex::indexOf)?.takeIf { it >= 0 })
    }
    var hasMovedSelection by remember { mutableStateOf(false) }
    SetupStepScaffold(step = 2, onBack = onBack, nextEnabled = selected != null, onNext = onNext) {
        SetupHeader("대화할 상대를 골라볼까요?", "누구와 대화할 지 선택해주세요.")
        Spacer(Modifier.height(24.dp))
        var bounds by remember { mutableStateOf<Map<Int, Pair<IntOffset, IntSize>>>(emptyMap()) }
        var gridOffset by remember { mutableStateOf(IntOffset.Zero) }
        var gridWindowOffset by remember { mutableStateOf(IntOffset.Zero) }
        val selectedBounds = selected?.let { bounds[it] }
        val density = LocalDensity.current
        val animatedOffset by animateIntOffsetAsState(
            targetValue = selectedBounds?.first ?: IntOffset.Zero,
            animationSpec = if (hasMovedSelection) tween(240, easing = FastOutSlowInEasing) else snap(),
            label = "setup2SelectionOffset",
        )
        val animatedSize by animateIntSizeAsState(
            targetValue = selectedBounds?.second ?: IntSize.Zero,
            animationSpec = if (hasMovedSelection) tween(240, easing = FastOutSlowInEasing) else snap(),
            label = "setup2SelectionSize",
        )
        val selectionAlpha by animateFloatAsState(
            targetValue = if (selected != null) 1f else 0f,
            animationSpec = tween(220, easing = FastOutSlowInEasing),
            label = "setup2SelectionAlpha",
        )
        Box(
            Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    val position = coordinates.positionInWindow()
                    gridWindowOffset = IntOffset(position.x.roundToInt(), position.y.roundToInt())
                },
        ) {
            bounds.forEach { (index, measured) ->
                val (offset, size) = measured
                val disabled = SetupPartnerRoleByIndex[index] in disabledOptions
                Box(
                    Modifier
                        .offset { offset }
                        .size(
                            with(density) { size.width.toDp() },
                            with(density) { size.height.toDp() },
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (disabled) Gray100 else Gray50)
                        .border(1.dp, Gray200, RoundedCornerShape(16.dp)),
                )
            }
            if (animatedSize != IntSize.Zero) {
                Box(
                    Modifier
                        .offset { animatedOffset }
                        .size(
                            with(density) { animatedSize.width.toDp() },
                            with(density) { animatedSize.height.toDp() },
                        )
                        .graphicsLayer { alpha = selectionAlpha }
                        .clip(RoundedCornerShape(16.dp))
                        .background(Primary100)
                        .border(1.dp, Primary600, RoundedCornerShape(16.dp)),
                )
            }
            Column(
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    val position = coordinates.positionInParent()
                    gridOffset = IntOffset(position.x.roundToInt(), position.y.roundToInt())
                },
                verticalArrangement = Arrangement.spacedBy(15.dp),
            ) {
                partnerOptions.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(15.dp),
                    ) {
                        rowItems.forEach { opt ->
                            val idx = partnerOptions.indexOf(opt)
                            val enabled = SetupPartnerRoleByIndex[idx] !in disabledOptions
                            PartnerCard(
                                option = opt,
                                selected = selected == idx,
                                enabled = enabled,
                                onClick = {
                                    if (selected != null && selected != idx) hasMovedSelection = true
                                    selected = idx
                                    onSelect(SetupPartnerRoleByIndex[idx])
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .onGloballyPositioned { coordinates ->
                                        val position = coordinates.positionInWindow()
                                        bounds = bounds + (
                                            idx to (
                                                IntOffset(
                                                    position.x.roundToInt() - gridWindowOffset.x,
                                                    position.y.roundToInt() - gridWindowOffset.y,
                                                ) to
                                                    coordinates.size
                                            )
                                        )
                                    },
                            )
                        }
                        if (rowItems.size == 1) Spacer(Modifier.weight(1f)) // 마지막 홀수 카드 반폭 유지
                    }
                }
            }
        }
    }
}

// ══════════════════ 화면 3 · 성별 · 나이 ══════════════════

@Composable
private fun PillOption(
    label: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Int,
    radius: Int,
) {
    val textColor by animateColorAsState(
        targetValue = when {
            !enabled -> Gray400
            selected -> Primary50
            else -> Gray800
        },
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "pillOptionTextColor",
    )
    // 칩만 카드와 반전 규칙이 다르다 — 선택 시 Purple/600으로 꽉 채우고 테두리를 없앤 뒤 글자를 Purple/50으로 뒤집는다.
    Box(
        modifier = modifier
            .height(height.dp)
            .clip(RoundedCornerShape(radius.dp))
            .background(Color.Transparent)
            .border(1.dp, Color.Transparent, RoundedCornerShape(radius.dp))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 23.dp), // CSS pill 안쪽 좌우 패딩 23. 고정폭 안 주면 글자만큼 hug(60대 이상=113 등)
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            // 선택되면 Body/L → Body/L Medium (400→500)까지 같이 바뀐다
            style = if (selected) TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma() else TqType.BodyL.figma(),
            color = textColor,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = TqType.TitleL.figma(), color = Gray800)
}

@Composable
fun ConversationSetup3Screen(
    onBack: () -> Unit = {},
    onNext: () -> Unit = {},
    onSelectGender: (MissionSetupPartnerGender) -> Unit = {},
    onSelectAgeGroup: (MissionSetupPartnerAgeGroup) -> Unit = {},
    initialGender: MissionSetupPartnerGender? = null,
    initialAgeGroup: MissionSetupPartnerAgeGroup? = null,
    disabledGenders: Set<MissionSetupPartnerGender> = emptySet(),
    disabledAgeGroups: Set<MissionSetupPartnerAgeGroup> = emptySet(),
) {
    var gender by remember(initialGender) {
        mutableStateOf(initialGender?.let(SetupGenderByIndex::indexOf)?.takeIf { it >= 0 })
    }
    var age by remember(initialAgeGroup) {
        mutableStateOf(initialAgeGroup?.let(SetupAgeGroupByIndex::indexOf)?.takeIf { it >= 0 })
    }
    SetupStepScaffold(step = 3, onBack = onBack, nextEnabled = gender != null && age != null, onNext = onNext) {
        SetupHeader("어떤 상대와 대화해볼까요?", "대화할 상대의 성별과 나이대를 선택해주세요.")
        Spacer(Modifier.height(36.dp))
        val density = LocalDensity.current
        Column(
            modifier = Modifier.padding(start = 6.dp),
            verticalArrangement = Arrangement.spacedBy(36.dp), // 성별 ↔ 나이 gap 36
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionTitle("성별")
                var genderBounds by remember { mutableStateOf<Map<Int, Pair<IntOffset, IntSize>>>(emptyMap()) }
                var genderMoved by remember { mutableStateOf(false) }
                val genderParent = remember { mutableStateOf(IntOffset.Zero) }
                val genderSelectedBounds = gender?.let { genderBounds[it] }
                val genderOffset by animateIntOffsetAsState(
                    genderSelectedBounds?.first ?: IntOffset.Zero,
                    if (genderMoved) tween(240, easing = FastOutSlowInEasing) else snap(),
                    label = "setup3GenderOffset",
                )
                val genderSize by animateIntSizeAsState(
                    genderSelectedBounds?.second ?: IntSize.Zero,
                    if (genderMoved) tween(240, easing = FastOutSlowInEasing) else snap(),
                    label = "setup3GenderSize",
                )
                val genderAlpha by animateFloatAsState(
                    if (gender != null) 1f else 0f,
                    tween(220, easing = FastOutSlowInEasing),
                    label = "setup3GenderAlpha",
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { c ->
                            val p = c.positionInWindow()
                            genderParent.value = IntOffset(p.x.roundToInt(), p.y.roundToInt())
                        },
                ) {
                    genderBounds.forEach { (index, measured) ->
                        val (offset, size) = measured
                        val disabled = SetupGenderByIndex[index] in disabledGenders
                        Box(
                            Modifier
                                .offset { offset }
                                .size(
                                    with(density) { size.width.toDp() },
                                    with(density) { size.height.toDp() },
                                )
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (disabled) Gray100 else Color.Transparent)
                                .border(1.dp, if (disabled) Gray200 else Gray300, RoundedCornerShape(18.dp)),
                        )
                    }
                    if (genderSize != IntSize.Zero) {
                        Box(
                            Modifier
                                .offset { genderOffset }
                                .size(with(density) { genderSize.width.toDp() }, with(density) { genderSize.height.toDp() })
                                .graphicsLayer { alpha = genderAlpha }
                                .clip(RoundedCornerShape(18.dp))
                                .background(Primary600),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        genderOptions.forEachIndexed { i, label ->
                            val enabled = SetupGenderByIndex[i] !in disabledGenders
                            PillOption(
                                label,
                                gender == i,
                                enabled,
                                {
                                    if (gender != null && gender != i) genderMoved = true
                                    gender = i
                                    onSelectGender(SetupGenderByIndex[i])
                                },
                                Modifier.width(130.dp).onGloballyPositioned { c ->
                                    val p = c.positionInWindow()
                                    genderBounds = genderBounds + (i to (IntOffset(p.x.roundToInt() - genderParent.value.x, p.y.roundToInt() - genderParent.value.y) to c.size))
                                },
                                height = 45,
                                radius = 18,
                            )
                        }
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionTitle("나이대")
                var ageBounds by remember { mutableStateOf<Map<Int, Pair<IntOffset, IntSize>>>(emptyMap()) }
                var ageMoved by remember { mutableStateOf(false) }
                val ageParent = remember { mutableStateOf(IntOffset.Zero) }
                val ageSelectedBounds = age?.let { ageBounds[it] }
                val ageOffset by animateIntOffsetAsState(
                    ageSelectedBounds?.first ?: IntOffset.Zero,
                    if (ageMoved) tween(240, easing = FastOutSlowInEasing) else snap(),
                    label = "setup3AgeOffset",
                )
                val ageSize by animateIntSizeAsState(
                    ageSelectedBounds?.second ?: IntSize.Zero,
                    if (ageMoved) tween(240, easing = FastOutSlowInEasing) else snap(),
                    label = "setup3AgeSize",
                )
                val ageAlpha by animateFloatAsState(
                    if (age != null) 1f else 0f,
                    tween(220, easing = FastOutSlowInEasing),
                    label = "setup3AgeAlpha",
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { c ->
                            val p = c.positionInWindow()
                            ageParent.value = IntOffset(p.x.roundToInt(), p.y.roundToInt())
                        },
                ) {
                    ageBounds.forEach { (index, measured) ->
                        val (offset, size) = measured
                        val disabled = SetupAgeGroupByIndex[index] in disabledAgeGroups
                        Box(
                            Modifier
                                .offset { offset }
                                .size(
                                    with(density) { size.width.toDp() },
                                    with(density) { size.height.toDp() },
                                )
                                .clip(RoundedCornerShape(24.dp))
                                .background(if (disabled) Gray100 else Color.Transparent)
                                .border(1.dp, if (disabled) Gray200 else Gray300, RoundedCornerShape(24.dp)),
                        )
                    }
                    if (ageSize != IntSize.Zero) {
                        Box(
                            Modifier
                                .offset { ageOffset }
                                .size(with(density) { ageSize.width.toDp() }, with(density) { ageSize.height.toDp() })
                                .graphicsLayer { alpha = ageAlpha }
                                .clip(RoundedCornerShape(24.dp))
                                .background(Primary600),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ageOptions.chunked(3).forEach { rowItems ->
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                rowItems.forEach { label ->
                                    val idx = ageOptions.indexOf(label)
                                    val enabled = SetupAgeGroupByIndex[idx] !in disabledAgeGroups
                                    val pillMod = if (idx == ageOptions.lastIndex) Modifier else Modifier.width(82.dp)
                                    PillOption(
                                        label,
                                        age == idx,
                                        enabled,
                                        {
                                            if (age != null && age != idx) ageMoved = true
                                            age = idx
                                            onSelectAgeGroup(SetupAgeGroupByIndex[idx])
                                        },
                                        pillMod.onGloballyPositioned { c ->
                                            val p = c.positionInWindow()
                                            ageBounds = ageBounds + (idx to (IntOffset(p.x.roundToInt() - ageParent.value.x, p.y.roundToInt() - ageParent.value.y) to c.size))
                                        },
                                        height = 40,
                                        radius = 24,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════ 화면 4 · 친밀도 · 말투 (점 척도 2개) ══════════════════
// 5점 점 척도: 안 선택 25 Gray300 / 선택 29 Primary600 + 글로우, 연결선 3 Gray300.

@Composable
private fun DotScale(
    value: Int,
    onValueChange: (Int) -> Unit,
    left: String,
    center: String,
    right: String,
    disabledValues: Set<Int> = emptySet(),
) {
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var displayedValue by remember { mutableIntStateOf(value) }
    var motionStart by remember { mutableIntStateOf(value) }
    var motionTarget by remember { mutableIntStateOf(value) }
    var isMoving by remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        if (!isMoving) displayedValue = value
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally, // CSS Frame 427321679 align center (트랙 313을 349 안 가운데)
        verticalArrangement = Arrangement.spacedBy(12.dp), // CSS Frame 427321679 gap 12
    ) {
        // 라벨 3개 (양끝 + 중앙) — Label/L Gray600
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val labelStyle = TqType.BodyM.copy(fontWeight = FontWeight.Medium).figma()
            Text(left, style = labelStyle, color = Color(0xFF475569))
            Text(center, style = labelStyle, color = Color(0xFF475569))
            Text(right, style = labelStyle, color = Color(0xFF475569))
        }
        // 회색 척도는 고정하고 보라색 선택점 하나만 선을 따라 이동한다.
        Box(modifier = Modifier.width(313.dp).height(29.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val selectedRadius = 14.5.dp.toPx()
                val idleRadius = 12.5.dp.toPx()
                val lineWidth = 3.dp.toPx()
                val firstX = selectedRadius
                val step = (size.width - selectedRadius * 2f) / 4f
                val centerY = size.height / 2f

                drawLine(
                    color = Gray300,
                    start = androidx.compose.ui.geometry.Offset(firstX, centerY),
                    end = androidx.compose.ui.geometry.Offset(firstX + step * 4f, centerY),
                    strokeWidth = lineWidth,
                    cap = StrokeCap.Round,
                )
                repeat(5) { index ->
                    drawCircle(
                        if (index in disabledValues) Gray200 else Gray300,
                        idleRadius,
                        androidx.compose.ui.geometry.Offset(firstX + step * index, centerY),
                    )
                }

                val sourceX = firstX + step * motionStart
                val targetX = firstX + step * motionTarget
                val displayedCenter = androidx.compose.ui.geometry.Offset(firstX + step * displayedValue, centerY)

                if (isMoving) {
                    val phase = progress.value
                    val lineGrow = (phase / 0.55f).coerceIn(0f, 1f)
                    val destinationGrow = ((phase - 0.55f) / 0.45f).coerceIn(0f, 1f)
                    val direction = if (targetX > sourceX) 1f else -1f
                    val sourceJointX = sourceX + direction * selectedRadius
                    val targetJointX = targetX - direction * selectedRadius
                    val lineHeadX = sourceJointX + (targetJointX - sourceJointX) * lineGrow
                    val lineTailX = sourceJointX + (targetJointX - sourceJointX) * destinationGrow

                    if (lineGrow > destinationGrow) {
                        drawLine(
                            color = Primary600,
                            start = androidx.compose.ui.geometry.Offset(lineTailX, centerY),
                            end = androidx.compose.ui.geometry.Offset(lineHeadX, centerY),
                            strokeWidth = lineWidth,
                            cap = StrokeCap.Round,
                        )
                    }

                    // 선이 도착점에 닿는 순간 출발 원이 완전히 선 안으로 빨려 들어간다.
                    val sourceRadius = selectedRadius * (1f - lineGrow)
                    if (sourceRadius > 0f) {
                        val sourceCenterX = sourceJointX - direction * sourceRadius
                        drawCircle(
                            Primary600,
                            sourceRadius,
                            androidx.compose.ui.geometry.Offset(sourceCenterX, centerY),
                        )
                    }

                    // 도착 원이 채워지는 동안 선의 출발점이 떨어져 도착점으로 함께 흡수된다.
                    val destinationRadius = selectedRadius * destinationGrow
                    if (destinationRadius > 0f) {
                        val destinationCenterX = targetJointX + direction * destinationRadius
                        drawCircle(
                            Primary600,
                            destinationRadius,
                            androidx.compose.ui.geometry.Offset(destinationCenterX, centerY),
                        )
                    }
                } else {
                    drawIntoCanvas { canvas ->
                        val glow = android.graphics.Paint().apply {
                            isAntiAlias = true
                            color = GlowPurple.copy(alpha = 0.6f).toArgb()
                            maskFilter = android.graphics.BlurMaskFilter(
                                12.dp.toPx(),
                                android.graphics.BlurMaskFilter.Blur.NORMAL,
                            )
                        }
                        canvas.nativeCanvas.drawCircle(displayedCenter.x, displayedCenter.y, selectedRadius, glow)
                    }
                    drawCircle(Primary600, selectedRadius, displayedCenter)
                }
            }

            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                for (i in 0 until 5) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clickable(
                                enabled = i !in disabledValues,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    if (!isMoving && i != displayedValue) {
                                        motionStart = displayedValue
                                        motionTarget = i
                                        isMoving = true
                                        scope.launch {
                                            progress.snapTo(0f)
                                            progress.animateTo(
                                                1f,
                                                animationSpec = tween(300, easing = FastOutSlowInEasing),
                                            )
                                            displayedValue = i
                                            onValueChange(i)
                                            isMoving = false
                                        }
                                    }
                                },
                            ),
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationSetup4Screen(
    onBack: () -> Unit = {},
    onNext: () -> Unit = {},
    onIntimacyChange: (Int) -> Unit = {}, // 눈금 index 0~4 그대로. 서버 1~5 변환은 모델이 한다.
    onFormalityChange: (Int) -> Unit = {},
    // 설정 저장 요청이 날아가는 동안 "대화 시작하기"를 잠근다(두 번 눌러 두 번 저장되는 것 방지).
    isSaving: Boolean = false,
    initialIntimacy: Int = 2,
    initialFormality: Int = 2,
    disabledIntimacy: Set<Int> = emptySet(),
    disabledFormality: Set<Int> = emptySet(),
) {
    var intimacy by remember(initialIntimacy) { mutableIntStateOf(initialIntimacy.coerceIn(0, 4)) }
    var tone by remember(initialFormality) { mutableIntStateOf(initialFormality.coerceIn(0, 4)) }
    SetupStepScaffold(step = 4, onBack = onBack, nextEnabled = !isSaving, onNext = onNext, nextText = "대화 시작하기") {
        SetupHeader("마지막으로,\n관계를 조금 더 정해볼까요?", "친밀도와 말투를 선택하면 준비가 끝나요.") // CSS 수동 줄바꿈: "마지막으로," 뒤 개행(2줄)
        Spacer(Modifier.height(36.dp))
        Column(
            modifier = Modifier.padding(start = 6.dp, end = 6.dp),
            verticalArrangement = Arrangement.spacedBy(36.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionTitle("관계 친밀도")
                DotScale(
                    intimacy,
                    { intimacy = it; onIntimacyChange(it) },
                    "매우 낯선 사이",
                    "보통",
                    "매우 친한 사이",
                    disabledIntimacy,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionTitle("대화 말투")
                DotScale(
                    tone,
                    { tone = it; onFormalityChange(it) },
                    "편한 말투",
                    "보통",
                    "격식있는 말투",
                    disabledFormality,
                )
            }
        }
    }
}

// ── Preview ──
@Preview(name = "설정1 장소", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun Setup1Preview() { TalkQQuestTheme { ConversationSetup1Screen() } }

@Preview(name = "설정2 상대", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun Setup2Preview() { TalkQQuestTheme { ConversationSetup2Screen() } }

@Preview(name = "설정3 성별·나이", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun Setup3Preview() { TalkQQuestTheme { ConversationSetup3Screen() } }

@Preview(name = "설정4 친밀도·말투", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun Setup4Preview() { TalkQQuestTheme { ConversationSetup4Screen() } }
