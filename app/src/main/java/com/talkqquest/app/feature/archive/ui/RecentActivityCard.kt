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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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

// 미션 난이도용 로컬 색상
private val OrangeText = Color(0xFFEF8F22)
private val EasyBg = Color(0xFFE3F4E0)
private val NormalBg = Color(0xFFFEF3E6)
private val HardBg = Color(0xFFFDE5E5)

internal fun String.keepWordsIntact(): String =
    replace(Regex("(?<=\\S)(?=\\S)"), "\u2060")

internal fun String.glueShortWords(): String =
    replace(Regex("(?<=(^|\\s)\\S) "), " ")

@Composable
internal fun RecentActivityCard(
    activity: RecentActivity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isMission = activity.type == ActivityType.MISSION

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isMission) 85.dp else 72.dp)
            .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .background(White)
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

        // 아이콘 우측 Gap (미션 8dp, 일반 12dp)
        Spacer(modifier = Modifier.width(if (isMission) 8.dp else 12.dp))

        // ==========================================
        // [중앙 영역] 텍스트 및 태그 컨테이너
        // ==========================================
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(if (isMission) 7.dp else 0.dp)
        ) {
            Text(
                text = activity.title.glueShortWords().keepWordsIntact(),
                modifier = Modifier.fillMaxWidth(),
                color = Gray900,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TqType.BodyL
            )

            if (isMission) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DifficultyLabel(difficulty = activity.difficulty ?: "쉬움")
                    CategoryTag(category = activity.category ?: "대화")
                    TimeXpRow(
                        minutes = activity.estimatedMinutes ?: 0,
                        xp = activity.rewardXp ?: 0
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(22.dp).padding(horizontal = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(start = 0.dp, top = 2.dp, end = 10.dp, bottom = 2.dp)
                    ) {
                        Text(text = activity.status, style = TqType.Caption, color = Gray500)
                    }

                    Box(
                        modifier = Modifier.size(width = 1.dp, height = 9.dp).background(Gray300)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(start = 10.dp, top = 2.dp, end = 0.dp, bottom = 2.dp)
                    ) {
                        Text(text = activity.date, style = TqType.Caption, color = Gray500)
                    }
                }
            }
        }

        // ==========================================
        // 💡 [핵심 추가] 우측 꺾쇠와의 Gap
        // 텍스트가 정확히 CSS 지정 길이(214dp / 236dp)에서 잘리도록 강제하는 반응형 Spacer
        // ==========================================
        Spacer(modifier = Modifier.width(if (isMission) 3.dp else 21.dp))

        // ==========================================
        // [우측 영역] 이동 꺾쇠 (Chevron)
        // ==========================================
        Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                // 💡 [수정됨] ic_forward_chevron 로컬 리소스로 교체
                painter = painterResource(id = R.drawable.ic_forward_chevron),
                contentDescription = "상세 보기",
                tint = Gray600,
            )
        }
    }
}

// ── 컴포넌트 하단부는 변경 사항 없으므로 기존 동일 유지 ──

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
        Text(text = difficulty, style = TqType.LabelM, color = textColor)
    }
}

@Composable
private fun CategoryTag(category: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Gray100)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = category, style = TqType.Caption, color = Gray500)
    }
}

@Composable
private fun TimeXpRow(minutes: Int, xp: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
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
            Text(text = "${minutes}분", style = TqType.Caption, color = Gray500, softWrap = false)
        }
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(9.dp)
                .background(Gray300),
        )
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp).height(22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_mission_xp),
                contentDescription = null,
            )
            Text(text = "${xp}XP", style = TqType.Caption, color = Gray500, softWrap = false)
        }
    }
}