package com.talkqquest.app.feature.archive.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.talkqquest.app.core.designsystem.Gray1000
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.softShadow
import com.talkqquest.app.feature.mission.ui.figma

// 💡 북마크 관리를 위한 공통 데이터 모델 (문장 & 리포트용)
data class BookmarkArchiveItem(
    val id: String,
    val title: String,
    val status: String,
    val date: String,
    val isSaved: Boolean = true,
    val memoKeywords: List<String> = emptyList(),
    val memoText: String = "",
    val relatedConversationId: String? = null,
    // 리포트일 때 서버가 준 종류 — "growth" | "weekly_compare" (GET /archives 응답의 reportType).
    // 예전에는 제목에 "주간 비교"가 들어있는지로 종류를 추측했는데, 서버 제목이
    // "4주차 비교 리포트"라 판정이 전부 어긋나 필터·라벨·상세 이동이 다 성장 리포트로 갔다.
    val reportType: String = "",
    // 정렬 전용 원본 시각(서버 createdAt, 시각 포함 ISO 문자열). date는 yyyy.MM.dd로 시각이
    // 잘려 있어 정렬용으로 쓰면 같은 날 항목이 전부 동률이 된다. 화면 표시는 date 그대로 쓰고
    // 정렬만 이 값으로 한다. 기본값 ""이라 다른 화면의 기존 생성 호출은 안 깨진다.
    val createdAtRaw: String = ""
) {
    val isWeeklyCompare: Boolean get() = reportType == "weekly_compare"
}

@Composable
fun BookmarkCard(
    item: BookmarkArchiveItem,
    isSentence: Boolean, // true면 문장용(따옴표, 문장 아이콘), false면 리포트용(리포트 아이콘)
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp) // 💡 섀도우 통일성 부여
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .background(Color.White)
            .padding(start = 16.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // [좌측] 💡 탭에 따른 아이콘 정확한 분기 처리 (문장 vs 리포트)
        // 💡 CSS 크기에 맞게 49.dp로 수정 및 불필요한 Box 제거
        Image(
            painter = painterResource(
                id = if (isSentence) R.drawable.img_archive_sentence else R.drawable.img_archive_report
            ),
            contentDescription = null,
            modifier = Modifier
                .size(49.dp)
                .clip(RoundedCornerShape(8.dp)),
        )

        Spacer(Modifier.width(12.dp))

        // [중앙] 텍스트 정보
        Column(modifier = Modifier.weight(1f)) {
            // 문장 탭일 경우 양옆에 큰따옴표 자동 추가
            val displayText = if (isSentence) "“${item.title}”" else item.title

            Text(
                text = displayText,
                style = TqType.BodyL.figma().copy(fontWeight = FontWeight.Medium),
                color = Gray900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            // 메타 정보 (상태 | 날짜)
            Row(
                modifier = Modifier.padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = item.status, style = TqType.Caption.figma(), color = Gray500)
                Spacer(Modifier.width(10.dp))
                Box(Modifier.width(1.dp).height(9.dp).background(Gray300))
                Spacer(Modifier.width(10.dp))
                Text(text = item.date, style = TqType.Caption.figma(), color = Gray500)
            }
        }

        Spacer(modifier = Modifier.width(21.dp))

        // [우측] 북마크 버튼
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null, // 클릭 시 물결 효과 제거
                    onClick = onToggleSave,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(
                    if (item.isSaved) R.drawable.ic_mission_bookmark_filled else R.drawable.ic_mission_bookmark
                ),
                contentDescription = if (item.isSaved) "저장 해제" else "저장",
                modifier = Modifier.size(24.dp) // 💡 CSS 크기에 맞게 24.dp로 명시적 지정
            )
        }
    }
}