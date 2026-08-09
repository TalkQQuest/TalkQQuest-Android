package com.talkqquest.app.feature.mission.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray200
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Primary50
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.Primary700
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.component.TqButton

// ── 미션 진입 · 대화 설정 4스텝 (UI 13차 "진입- 대화 설정 1~4" 전사) ──
// 미션 상세 "다음" → 1(장소)→2(상대)→3(성별·나이)→4(친밀도·말투) → 대화. 옛 ConversationPrepScreen 대체.
//
// ★옵션 라벨·아이콘·세부설명은 피그마 실렌더(스크린샷) 기준. CSS 레이어명은 플레이스홀더라 못 씀.
// ★아이콘 = 디자이너 SVG를 vector drawable로 변환(ic_setup_*).
// ★선택값은 대화 시작 API에 넣을 서버 필드가 아직 없어 화면 로컬 상태만(흐름·네비만 동작).
// ★카드 선택 상태 색(Primary600 테두리 + Primary50 배경)은 디자인에 없어 앱 관례로.

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
                        .clickable(onClick = onBack),
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

private fun cardBorder(selected: Boolean) = if (selected) 1.5.dp else 1.dp
private fun cardBorderColor(selected: Boolean) = if (selected) Primary600 else Gray200
private fun cardBg(selected: Boolean) = if (selected) Primary50 else Gray50

// ══════════════════ 화면 1 · 장소 (세로 리스트) ══════════════════
// 카드: 아이콘(40 원형) + 제목 + 선택적 세부설명. CSS 361x76, radius 16, gap 12.

@Composable
private fun PlaceCard(option: PlaceOption, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg(selected))
            .border(cardBorder(selected), cardBorderColor(selected), RoundedCornerShape(16.dp))
            .clickable(
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
                .background(Gray200), // CSS Frame 427321657 배경 = Gray/200(#E2E8F0)
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(option.icon),
                contentDescription = null,
                tint = Gray500, // CSS Vector #64748B(Gray/500). 옆 글자색 관성으로 Gray700 넣던 것 수정
                // 크기 미지정 = 벡터 native(학교 27×20 등) 그대로 = CSS Vector 크기와 일치.
                // 정사각 size()로 우기면 비정사각 글리프가 비율맞춤으로 작아짐(작게 보이던 원인)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = option.label, style = TqType.TitleL.figma(), color = Gray800)
            if (option.subtitle != null) {
                Text(text = option.subtitle, style = TqType.BodyM.figma(), color = Gray500)
            }
        }
    }
}

@Composable
fun ConversationSetup1Screen(onBack: () -> Unit = {}, onNext: () -> Unit = {}) {
    var selected by remember { mutableStateOf<Int?>(null) }
    SetupStepScaffold(step = 1, onBack = onBack, nextEnabled = selected != null, onNext = onNext) {
        SetupHeader("어디에서 나눌 대화인가요?", "대화 장소를 선택해주세요.")
        Spacer(Modifier.height(24.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            placeOptions.forEachIndexed { i, opt ->
                PlaceCard(option = opt, selected = selected == i, onClick = { selected = i })
            }
        }
    }
}

// ══════════════════ 화면 2 · 상대 (2열 그리드) ══════════════════
// 카드: 173x90, radius 16, 아이콘(30) 위 + 라벨 아래. 행 gap 17 · 열 gap 15.

@Composable
private fun PartnerCard(option: IconOption, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(90.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg(selected))
            .border(cardBorder(selected), cardBorderColor(selected), RoundedCornerShape(16.dp))
            .clickable(
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
            tint = Gray500, // CSS Vector #64748B(Gray/500)
            // 크기 미지정 = 벡터 native(친구 25×19 등) = CSS Vector(30 슬롯 안 글리프). 정사각 size(30)이면 비율 왜곡
        )
        Spacer(Modifier.height(4.dp)) // CSS Frame 427321664 gap 4
        Text(text = option.label, style = TqType.TitleL.figma(), color = Gray800) // CSS 상대 라벨 = 18/600 (Title/L)
    }
}

@Composable
fun ConversationSetup2Screen(onBack: () -> Unit = {}, onNext: () -> Unit = {}) {
    var selected by remember { mutableStateOf<Int?>(null) }
    SetupStepScaffold(step = 2, onBack = onBack, nextEnabled = selected != null, onNext = onNext) {
        SetupHeader("대화할 상대를 골라볼까요?", "누구와 대화할 지 선택해주세요.")
        Spacer(Modifier.height(24.dp))
        Column(verticalArrangement = Arrangement.spacedBy(15.dp)) { // 행 gap 15(≈17)
            partnerOptions.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(15.dp),
                ) {
                    rowItems.forEach { opt ->
                        val idx = partnerOptions.indexOf(opt)
                        PartnerCard(
                            option = opt,
                            selected = selected == idx,
                            onClick = { selected = idx },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (rowItems.size == 1) Spacer(Modifier.weight(1f)) // 마지막 홀수 카드 반폭 유지
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Int,
    radius: Int,
) {
    Box(
        modifier = modifier
            .height(height.dp)
            .clip(RoundedCornerShape(radius.dp))
            .background(if (selected) Primary50 else Color.Transparent)
            .border(cardBorder(selected), cardBorderColor(selected), RoundedCornerShape(radius.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 23.dp), // CSS pill 안쪽 좌우 패딩 23. 고정폭 안 주면 글자만큼 hug(60대 이상=113 등)
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, style = TqType.BodyL.figma(), color = if (selected) Primary700 else Gray800)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = TqType.TitleL.figma(), color = Gray800)
}

@Composable
fun ConversationSetup3Screen(onBack: () -> Unit = {}, onNext: () -> Unit = {}) {
    var gender by remember { mutableStateOf<Int?>(null) }
    var age by remember { mutableStateOf<Int?>(null) }
    SetupStepScaffold(step = 3, onBack = onBack, nextEnabled = gender != null && age != null, onNext = onNext) {
        SetupHeader("어떤 상대와 대화해볼까요?", "대화할 상대의 성별과 나이대를 선택해주세요.")
        Spacer(Modifier.height(36.dp))
        Column(
            modifier = Modifier.padding(start = 6.dp),
            verticalArrangement = Arrangement.spacedBy(36.dp), // 성별 ↔ 나이 gap 36
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionTitle("성별")
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    genderOptions.forEachIndexed { i, label ->
                        PillOption(label, gender == i, { gender = i }, Modifier.width(130.dp), height = 45, radius = 18)
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionTitle("나이대")
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ageOptions.chunked(3).forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowItems.forEach { label ->
                                val idx = ageOptions.indexOf(label)
                                // 10~50대는 최대폭(82) 고정으로 통일 → 열 정렬. 60대 이상(마지막)만 hug — 맨 끝이라 오른쪽으로 나가도 정렬 영향 없음
                                val pillMod = if (idx == ageOptions.lastIndex) Modifier else Modifier.width(82.dp)
                                PillOption(label, age == idx, { age = idx }, pillMod, height = 40, radius = 24)
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
private fun DotScale(value: Int, onValueChange: (Int) -> Unit, left: String, center: String, right: String) {
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
        // 점·선 트랙 (높이 29 = 선택 점 기준)
        Row(
            modifier = Modifier.width(313.dp).height(29.dp), // CSS 척도 트랙 폭 313 (라벨 349보다 좁게, 가운데)
            verticalAlignment = Alignment.CenterVertically,
        ) {
            for (i in 0 until 5) {
                val on = i == value
                Box(
                    modifier = Modifier
                        .size(if (on) 29.dp else 25.dp)
                        // 선택 dot 글로우 = CSS box-shadow: 0 0 12px rgba(114,100,248,0.6).
                        // Modifier.shadow(입체 그림자)로는 못 그려서(색 안 먹고 방향성) BlurMaskFilter로 뒤에 번지는 보라 원을 직접 그림.
                        .then(
                            if (on) Modifier.drawBehind {
                                val glow = android.graphics.Paint().apply {
                                    isAntiAlias = true
                                    color = GlowPurple.copy(alpha = 0.6f).toArgb()
                                    maskFilter = android.graphics.BlurMaskFilter(12.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
                                }
                                drawIntoCanvas { it.nativeCanvas.drawCircle(size.width / 2f, size.height / 2f, size.minDimension / 2f, glow) }
                            } else Modifier,
                        )
                        .clip(CircleShape)
                        .background(if (on) Primary600 else Gray300)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onValueChange(i) },
                        ),
                )
                if (i < 4) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .background(Gray300), // 연결선은 항상 Gray300 (CSS)
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationSetup4Screen(onBack: () -> Unit = {}, onNext: () -> Unit = {}) {
    var intimacy by remember { mutableIntStateOf(2) } // 기본 중앙(보통)
    var tone by remember { mutableIntStateOf(2) }
    SetupStepScaffold(step = 4, onBack = onBack, nextEnabled = true, onNext = onNext, nextText = "대화 시작하기") {
        SetupHeader("마지막으로,\n관계를 조금 더 정해볼까요?", "친밀도와 말투를 선택하면 준비가 끝나요.") // CSS 수동 줄바꿈: "마지막으로," 뒤 개행(2줄)
        Spacer(Modifier.height(36.dp))
        Column(
            modifier = Modifier.padding(start = 6.dp, end = 6.dp),
            verticalArrangement = Arrangement.spacedBy(36.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionTitle("관계 친밀도")
                DotScale(intimacy, { intimacy = it }, "매우 낯선 사이", "보통", "매우 친한 사이")
            }
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionTitle("대화 말투")
                DotScale(tone, { tone = it }, "편한 말투", "보통", "격식있는 말투")
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
