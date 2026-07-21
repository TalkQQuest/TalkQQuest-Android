package com.talkqquest.app.feature.archive.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.Success
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.softShadow
import com.talkqquest.app.feature.mission.ui.figma

// 🚨 feature.mission 참조 완벽 제거!

// 💡 보관함 전용 독립 데이터 모델 생성
// 기존 ArchiveMissionItem 데이터 클래스 맨 끝에 날짜 속성 추가!
data class ArchiveMissionItem(
    val id: Long,
    val title: String,
    val category: String,
    val difficulty: String,
    val duration: Int,
    val xp: Int,
    val isCompleted: Boolean,
    val isSaved: Boolean,
    val completedDate: String = "" // 💡 UI에는 안 보이지만 정렬을 위해 추가된 날짜!
)

// 로컬 색상 (이 파일 안에서만 안전하게 사용)
private val OrangeText = Color(0xFFEF8F22)
private val EasyBg = Color(0xFFE3F4E0)
private val NormalBg = Color(0xFFFEF3E6)
private val HardBg = Color(0xFFFDE5E5)

@Composable
fun ArchiveMissionCard(
    mission: ArchiveMissionItem, // 완전히 독립된 모델 사용
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(85.dp)
            .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .background(White)
            .padding(start = 16.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // [좌측 영역] 미션 전용 아이콘
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_archive_mission),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // [중앙 영역] 텍스트 및 태그
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                text = mission.title,
                modifier = Modifier.fillMaxWidth(),
                color = Gray900,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TqType.BodyL.figma()
            )

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ArchiveDifficultyLabel(difficulty = mission.difficulty)
                ArchiveCategoryTag(category = mission.category)
                ArchiveTimeXpRow(minutes = mission.duration, xp = mission.xp)
            }
        }

        Spacer(modifier = Modifier.width(3.dp))

        // [우측 영역] 보관함 전용 북마크 버튼
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggleSave
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(
                    if (mission.isSaved) R.drawable.ic_mission_bookmark_filled else R.drawable.ic_mission_bookmark
                ),
                contentDescription = if (mission.isSaved) "북마크 해제" else "북마크",
            )
        }
    }
}

// ── 내부용(Private) 서브 컴포넌트 ──
@Composable
private fun ArchiveDifficultyLabel(difficulty: String) {
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

@Composable
private fun ArchiveCategoryTag(category: String) {
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

@Composable
private fun ArchiveTimeXpRow(minutes: Int, xp: Int) {
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
            Text(text = "${minutes}분", style = TqType.Caption.figma(), color = Gray500, softWrap = false)
        }
        Box(modifier = Modifier.width(1.dp).height(9.dp).background(Gray300))
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp).height(22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Image(painter = painterResource(R.drawable.ic_mission_xp), contentDescription = null)
            Text(text = "${xp}XP", style = TqType.Caption.figma(), color = Gray500, softWrap = false)
        }
    }
}