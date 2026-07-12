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
    // 💡 TqCard 대신 Row + softShadow 조합으로 재설계했습니다.
    // 카드 전체 영역 (클릭 가능)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .softShadow(color = Gray1000.copy(alpha = 0.04f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .background(White)
            // 💡 상하 14dp, 좌측 16dp, 우측 13dp로 내부 여백을 조율했습니다.
            .padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 13.dp),
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

        // 아이콘 영역 (48x48 컨테이너 내 40x40 이미지)
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        // ==========================================
        // [중앙 영역] 텍스트 및 태그 컨테이너
        // ==========================================
        Column(
            // 💡 불필요하게 걸려있던 padding(end=8.dp)를 없애 텍스트가 남은 가로폭을 전부 100% 사용하게 합니다.
            // (지난번에 주석만 달고 안 지웠던 패딩을 이번에 확실히 제거하여 가로폭 추가 확보!)
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(7.dp) // 제목 ↔ 메타줄 (CSS Frame 350 gap)
        ) {
            // 제목 CSS: font 16px, weight 500, color #1E293B
            Text(
                // 💡 단어 잘림 방지 로직 적용
                text = activity.title.glueShortWords().keepWordsIntact(),
                // CSS: Body/L Medium (16/24, 굵기 500)
                style = TqType.BodyL.figma().copy(fontWeight = FontWeight.Medium, lineBreak = LineBreak.Heading),
                color = Gray900,
                maxLines = 1,
                overflow = TextOverflow.Visible // 💡 극도로 좁은 기기에서 화면을 뚫고 나갈 때만 ... 표기
            )

            // 💡 타입이 MISSION이면 미션 전용 태그(난이도, 카테고리, 시간, XP) 렌더링
            if (activity.type == ActivityType.MISSION) {
                Row(
                    // 좁은 화면에서 메타줄이 안 들어가면 글자를 꺾지 말고 가로 스크롤
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp) // 알약 간격 (CSS Frame 349/348 gap)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 상태 텍스트
                    Text(
                        text = activity.status,
                        style = TqType.Caption.figma(),
                        color = Gray500
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // 구분선 (CSS: 1x9px, color #CBD5E1)
                    Box(
                        modifier = Modifier
                            .size(width = 1.dp, height = 9.dp)
                            .background(Gray300)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // 날짜 텍스트
                    Text(
                        text = activity.date,
                        style = TqType.Caption.figma(),
                        color = Gray500
                    )
                }
            }
        }

        // 💡 텍스트와 쉐브론 사이 간격을 4dp로 최소화하여 텍스트 영역 극대화
        Spacer(modifier = Modifier.width(4.dp))

        // ==========================================
        // [우측 영역] 이동 꺾쇠 (Chevron)
        // ==========================================
        // 💡 항상 우측 꺾쇠 노출 (북마크 X)
        // 💡 44x44 터치 영역 규격 적용
        Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "상세 보기",
                tint = Gray600,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ── 미션 전용 태그 컴포넌트 ──

/**
 * 난이도 알약 (CSS Frame 345): radius 16, padding 4x12, Label/M. 색은 난이도별(CSS 원본값).
 */
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

/**
 * 카테고리 태그 (CSS Frame 342): radius 16, padding 4x12, Gray100 배경, Caption Gray500.
 */
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

/**
 * 시간 · 구분선 · XP (CSS Frame 347: [시계 9 + N분] | 1x9 구분선 | [플러스 + NXP], 안쪽 여백 10)
 */
@Composable
private fun TimeXpRow(minutes: Int, xp: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(
            // 💡 원본 규격 복구 (horizontal = 10.dp)
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
