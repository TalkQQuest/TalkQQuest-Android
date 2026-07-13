package com.talkqquest.app.feature.archive.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.Error
import com.talkqquest.app.core.designsystem.Gray100
import com.talkqquest.app.core.designsystem.Gray1000
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.Success
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.softShadow
import com.talkqquest.app.feature.archive.viewmodel.ActivityType
import com.talkqquest.app.feature.archive.viewmodel.RecentActivity
import com.talkqquest.app.feature.mission.ui.figma // 로컬 도구 재사용

// 미션 난이도용 로컬 색상
private val OrangeText = Color(0xFFEF8F22) // CSS: ORANGE (보통)
private val EasyBg = Color(0xFFE3F4E0)     // 쉬움 알약 배경
private val NormalBg = Color(0xFFFEF3E6)   // 보통 알약 배경
private val HardBg = Color(0xFFFDE5E5)     // 어려움 알약 배경

// 어절 안 글자 사이에 WORD JOINER(U+2060)를 끼워 어절 중간 줄바꿈("사/람에게")을 막음 (전 버전 동작).
internal fun String.keepWordsIntact(): String =
    replace(Regex("(?<=\\S)(?=\\S)"), "\u2060")

// 한 글자 어절("한 번"의 "한")이 줄 끝에 홀로 남지 않게 NBSP로 다음 어절과 묶음.
internal fun String.glueShortWords(): String =
    replace(Regex("(?<=(^|\\s)\\S) "), " ")

/**
 * 1. 보관함 최근 활동 카드 컴포넌트
 * 전달받은 `activity.type`에 따라 내부 레이아웃을 다르게 그립니다.
 * - MISSION 타입: 제목 하단에 난이도, 카테고리, 시간, 경험치 태그 표시
 * - 그 외 타입: 제목 하단에 상태(예: 미션 완료) 및 날짜 텍스트 표시
 */
@Composable
internal fun RecentActivityCard(
    activity: RecentActivity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isMission = activity.type == ActivityType.MISSION

    // 카드 전체 영역 (클릭 가능)
    Row(
        modifier = modifier
            .fillMaxWidth()
            // 💡 CSS Frame 427321227(미션 85px) vs Frame 427321181(일반 72px)
            .height(if (isMission) 85.dp else 72.dp)
            // 💡 CSS box-shadow: rgba(15, 23, 42, 0.01)
            .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .background(White)
            // 💡 CSS padding: 0 6px 0 16px (CenterVertically가 상하를 자동 중앙 정렬해 줌)
            .padding(start = 16.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // ==========================================
        // [좌측 영역] 아이콘
        // ==========================================
        val iconRes = when (activity.type) {
            ActivityType.MISSION -> R.drawable.img_archive_mission
            ActivityType.CONVERSATION -> R.drawable.img_archive_conversation
            ActivityType.SENTENCE -> R.drawable.img_archive_sentence
            ActivityType.REPORT -> R.drawable.img_archive_report
        }

        // 아이콘 영역 (💡 CSS Frame 427321186/427321187: 48x48)
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        }

        // 💡 CSS Frame 427321225(미션) gap: 8px, Frame 427321179(일반) gap: 12px
        Spacer(modifier = Modifier.width(if (isMission) 8.dp else 12.dp))

        // ==========================================
        // [중앙 영역] 텍스트 및 태그 컨테이너
        // ==========================================
        Column(
            modifier = Modifier.weight(1f),
            // 💡 CSS Frame 350(미션) gap: 7px, 일반은 0px
            verticalArrangement = Arrangement.spacedBy(if (isMission) 7.dp else 0.dp)
        ) {
            // 제목 CSS: font 16px, weight 500, color #1E293B
            Text(
                // 단어 잘림 방지 로직 적용
                text = activity.title.glueShortWords().keepWordsIntact(),
                // CSS: Body/L Medium (16/24, 굵기 500)
                style = TqType.BodyL.figma().copy(fontWeight = FontWeight.Medium, lineBreak = LineBreak.Heading),
                color = Gray900,
                maxLines = 1,
                overflow = TextOverflow.Visible // 극도로 좁은 기기에서 화면을 뚫고 나갈 때만 ... 표기
            )

            // 💡 타입이 MISSION이면 미션 전용 태그(난이도, 카테고리, 시간, XP) 렌더링
            if (isMission) {
                Row(
                    // 좁은 화면에서 메타줄이 안 들어가면 글자를 꺾지 말고 가로 스크롤
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    // 💡 CSS Frame 349 gap: 8px
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DifficultyLabel(difficulty = activity.difficulty ?: "쉬움")
                    CategoryTag(category = activity.category ?: "대화")
                    TimeXpRow(
                        minutes = activity.estimatedMinutes ?: 0,
                        xp = activity.rewardXp ?: 0
                    )
                }
            }
            // 💡 그 외 타입(대화, 문장, 리포트)은 기존의 상태 | 날짜 렌더링
            else {
                // 💡 CSS Frame 347: height 22px, padding 0 2px
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(22.dp).padding(horizontal = 2.dp)
                ) {
                    // 상태 텍스트 (💡 CSS Frame 343: padding 2 10 2 0)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(start = 0.dp, top = 2.dp, end = 10.dp, bottom = 2.dp)
                    ) {
                        Text(
                            text = activity.status,
                            style = TqType.Caption.figma(),
                            color = Gray500
                        )
                    }

                    // 구분선 (💡 CSS: 1x9px, color #CBD5E1)
                    Box(
                        modifier = Modifier
                            .size(width = 1.dp, height = 9.dp)
                            .background(Gray300)
                    )

                    // 날짜 텍스트 (💡 CSS Frame 344: padding 2 0 2 10)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(start = 10.dp, top = 2.dp, end = 0.dp, bottom = 2.dp)
                    ) {
                        Text(
                            text = activity.date,
                            style = TqType.Caption.figma(),
                            color = Gray500
                        )
                    }
                }
            }
        }

        // ==========================================
        // [우측 영역] 이동 꺾쇠 (Chevron)
        // ==========================================
        // 💡 CSS chevoren_left: 44x44
        Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "상세 보기",
                tint = Gray600,
            )
        }
    }
}

// ── 미션 전용 태그 컴포넌트 ──

/**
 * 난이도 알약 (💡 CSS Frame 345): radius 16, padding 4x12, Label/M, height 26.
 */
@Composable
private fun DifficultyLabel(difficulty: String) {
    val (textColor, bgColor) = when (difficulty) {
        "쉬움" -> Success to EasyBg
        "보통" -> OrangeText to NormalBg
        "어려움" -> Error to HardBg
        else -> Gray700 to Gray100
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = difficulty, style = TqType.LabelM.figma(), color = textColor)
    }
}

/**
 * 카테고리 태그 (💡 CSS Frame 342): radius 16, padding 4x12, Gray100 배경, Caption Gray500, height 26.
 */
@Composable
private fun CategoryTag(category: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Gray100)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = category, style = TqType.Caption.figma(), color = Gray500)
    }
}

/**
 * 시간 · 구분선 · XP (💡 CSS Frame 347: [시계 9 + N분] | 1x9 구분선 | [플러스 + NXP])
 */
@Composable
private fun TimeXpRow(minutes: Int, xp: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // 💡 CSS Frame 343 원본 (horizontal = 10.dp, height = 22.dp)
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp).height(22.dp),
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
        // 💡 CSS Frame 344 원본 (horizontal = 10.dp, height = 22.dp)
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp).height(22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_mission_xp),
                contentDescription = null,
                modifier = Modifier.size(11.dp), // CSS Frame 344 기준 11x11
            )
            Text(text = "${xp}XP", style = TqType.Caption.figma(), color = Gray500, softWrap = false)
        }
    }
}
