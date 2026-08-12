package com.talkqquest.app.feature.mission.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.Error
import com.talkqquest.app.core.designsystem.Gray100
import com.talkqquest.app.core.designsystem.Gray1000
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.Success
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.softShadow
import com.talkqquest.app.feature.mission.data.model.MissionListItem
import androidx.compose.material3.Text

// ── 미션 카드 (UI 1차 v2.css Frame 352 전사) ──
// 미션 목록·저장 시트(MissionSaveSheet)가 같은 카드를 써서 별도 파일로 분리.
// 화면 로컬 도구(softShadow·figma()·줄바꿈)도 카드와 함께 이동 — 같은 패키지의 화면들이 공유.

// 디자인 외 신규색(난이도 알약) — CSS 원본값 그대로, 디자인시스템에 없어 화면 로컬 정의.
internal val OrangeText = Color(0xFFEF8F22) // CSS: ORANGE (보통)
internal val EasyBg = Color(0xFFE3F4E0)     // 쉬움 알약 배경
internal val NormalBg = Color(0xFFFEF3E6)   // 보통 알약 배경
internal val HardBg = Color(0xFFFDE5E5)     // 어려움 알약 배경

// 미션 카드: 흰 배경, radius 20, 그림자 0 8 24 rgba(15,23,42,0.01), padding 14/20.
// 높이 85 = 제목 1줄일 때. 제목이 길면 줄바꿈되고 카드가 늘어남(말줄임 없음 — 팀 결정).
// 카드 전체 = 미션 상세로 가는 버튼. 북마크만 예외(자기 클릭 소비).
//
// showTarget: 카드 왼쪽 과녁 아이콘 (UI v4 신규). 붙는 곳이 정해져 있음 —
//   O: 저장 목록 화면, 저장 시트의 "저장 목록" 섹션 카드
//   X: 미션 목록 본 화면, 저장 시트의 "저장됨"(방금 저장한) 카드
// 과녁이 있으면 카드 좌우 padding도 CSS가 다름(16/6 ↔ 20/9).
@Composable
internal fun MissionCard(
    mission: MissionListItem,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier,
    showTarget: Boolean = false,
    animateContentChanges: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .background(White)
            // 과녁 카드 padding 0 6 0 16 / 기본 카드 padding 20·9 (CSS). 위아래 14 = (85-57)/2.
            .padding(
                start = if (showTarget) 16.dp else 20.dp,
                end = if (showTarget) 6.dp else 9.dp,
                top = 14.dp,
                bottom = 14.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showTarget) {
            // 과녁: 48 컨테이너 안에 40 이미지 중앙 (CSS Frame 427321186 48x48 / 이미지 40x40 left4 top4)
            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                Image(
                    // 저장 시트 카드 과녁 = 보관함 아이콘과 동일(디자인: 시트·보관함 같은 04_21_46 아이콘)
                    painter = painterResource(R.drawable.img_archive_mission),
                    contentDescription = null, // 장식 — 카드 제목이 이미 미션을 설명함
                    modifier = Modifier.size(48.dp), // 디자인: 아이콘이 49px 슬롯을 꽉 채움(옛 40 → 48)
                )
            }
            Spacer(Modifier.width(8.dp)) // 과녁 ↔ 텍스트 (CSS Frame 427321225 gap 8)
        }
        if (animateContentChanges) {
            AnimatedContent(
                // 필터 전환 때만 제목·메타를 바꾸기 위한 전환이다. 북마크 여부까지 target에
                // 넣으면 아이콘을 누를 때도 이전·새 카드 본문이 동시에 그려져 깜빡여 보인다.
                targetState = mission.copy(isSaved = false),
                transitionSpec = {
                    (fadeIn(tween(260, easing = FastOutSlowInEasing)) +
                        slideInVertically(
                            animationSpec = tween(260, easing = FastOutSlowInEasing),
                            initialOffsetY = { it / 12 },
                        )) togetherWith
                        (fadeOut(tween(260, easing = FastOutSlowInEasing)) +
                            slideOutVertically(
                                animationSpec = tween(260, easing = FastOutSlowInEasing),
                                targetOffsetY = { -it / 12 },
                            ))
                },
                modifier = Modifier.weight(1f),
                label = "missionCardContent",
            ) { animatedMission ->
                MissionCardBody(
                    mission = animatedMission,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            MissionCardBody(
                mission = mission,
                modifier = Modifier.weight(1f),
            )
        }
        // 북마크는 카드 내용 전환과 독립시켜, 저장 여부가 바뀌어도 아이콘만 즉시 교체한다.
        BookmarkButton(isSaved = mission.isSaved, onToggleSave = onToggleSave)
    }
}

@Composable
private fun MissionCardBody(
    mission: MissionListItem,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(7.dp), // 제목 ↔ 메타줄 (CSS Frame 350 gap)
        ) {
            Text(
                text = mission.title.glueShortWords(),
                // CSS: Body/L Medium (16/24, 굵기 500). AI 피드백 상세와 같은 어절 단위 줄바꿈:
                // 문장 전체를 균형 있게 배치하고, 한 글자 어절이 줄 끝에 홀로 남지 않게 한다.
                style = TqType.BodyL.figma().copy(
                    fontWeight = FontWeight.Medium,
                    lineBreak = LineBreak(
                        strategy = LineBreak.Strategy.HighQuality,
                        strictness = LineBreak.Strictness.Strict,
                        wordBreak = LineBreak.WordBreak.Phrase,
                    ),
                ),
                color = Gray900,
            )
            Row(
                // 좁은 화면에서 메타줄이 안 들어가면 글자를 꺾지 말고 가로 스크롤 (393에선 다 들어가 변화 없음)
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp), // 알약 간격 (CSS Frame 349/348 gap)
            ) {
                DifficultyLabel(difficulty = mission.difficulty)
                CategoryTag(category = mission.category)
                TimeXpRow(minutes = mission.estimatedMinutes, xp = mission.rewardXp)
            }
        }
    }
}

@Composable
private fun BookmarkButton(isSaved: Boolean, onToggleSave: () -> Unit) {
    // 리플(사각형 번쩍임) 없이 아이콘만 상태를 바꾼다.
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
                if (isSaved) R.drawable.ic_mission_bookmark_filled else R.drawable.ic_mission_bookmark,
            ),
            contentDescription = if (isSaved) "북마크 해제" else "북마크",
        )
    }
}

// 난이도 알약 (CSS Frame 345): radius 16, padding 4x12, Label/M. 색은 난이도별(CSS 원본값).
@Composable
private fun DifficultyLabel(difficulty: String) {
    val (textColor, bgColor) = when (difficulty) {
        "쉬움" -> Success to EasyBg
        "보통" -> OrangeText to NormalBg
        "어려움" -> Error to HardBg
        else -> Gray700 to Gray100 // 알 수 없는 값 대비(서버 값 변동 방어)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(text = difficulty, style = TqType.LabelM.figma(), color = textColor)
    }
}

// 카테고리 태그 (CSS Frame 342): radius 16, padding 4x12, Gray100 배경, Caption Gray500.
@Composable
private fun CategoryTag(category: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Gray100)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(text = category, style = TqType.Caption.figma(), color = Gray500)
    }
}

// 시간 · 구분선 · XP (CSS Frame 347: [시계 9 + N분] | 1x9 구분선 | [플러스 + NXP], 안쪽 여백 10)
@Composable
private fun TimeXpRow(minutes: Int, xp: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_mission_time),
                contentDescription = null,
                modifier = Modifier.size(9.dp),
            )
            Text(text = "${minutes}분", style = TqType.Caption.figma(), color = Gray500, softWrap = false)
        }
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(9.dp)
                .background(Gray300),
        )
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_mission_xp),
                contentDescription = null,
                modifier = Modifier.size(7.dp),
            )
            Text(text = "${xp}XP", style = TqType.Caption.figma(), color = Gray500, softWrap = false)
        }
    }
}

// 소프트 그림자는 core/designsystem의 공통 softShadow 사용 (로컬 복사본 정리됨).

// Compose가 깎는 line-height 위아래 여백을 피그마처럼 살림.
private val FullLeading = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

internal fun TextStyle.figma(): TextStyle = copy(lineHeightStyle = FullLeading)

// 어절 안 글자 사이에 WORD JOINER(U+2060)를 끼워 어절 중간 줄바꿈("사/람에게")을 막음 (전 버전 동작).
internal fun String.keepWordsIntact(): String =
    replace(Regex("(?<=\\S)(?=\\S)"), "⁠")

// 한 글자 어절("한 번"의 "한")이 줄 끝에 홀로 남지 않게 NBSP로 다음 어절과 묶음.
internal fun String.glueShortWords(): String =
    replace(Regex("(?<=(^|\\s)\\S) "), " ")
