package com.talkqquest.app.feature.archive.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.component.TqChoiceBottomSheet
import com.talkqquest.app.feature.mission.ui.figma

@Composable
fun ArchiveReportBottomSheet(
    isVisible: Boolean,
    currentFilter: String,
    onFilterSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    TqChoiceBottomSheet(
        visible = isVisible,
        onDismiss = onDismissRequest,
        cornerRadius = 36.dp,
        // CSS 패딩(상단 28px)을 정확히 맞추기 위해 드래그 핸들은 숨김 처리
        dragHandle = false,
    ) {
        // 💡 CSS 명세 반영: padding: 28px 16px 0px 20px; gap: 16px;
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 28.dp, end = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val filters = listOf("전체", "성장 리포트", "주간 비교 리포트")

            filters.forEach { filter ->
                val isSelected = currentFilter == filter

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        // CSS 명세는 height 24px 이지만, 터치 영역(클릭)을 원활하게 하기 위해 상하 패딩 추가
                        .clickable { onFilterSelected(filter) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = filter,
                        // 💡 CSS 명세 반영: Body/L Medium, Color #273449 (Gray800)
                        style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(),
                        color = Gray800
                    )

                    if (isSelected) {
                        // 💡 CSS 명세 반영: Icon Border Color #273449 (Gray800)
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "선택됨",
                            tint = Gray800,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        // 선택되지 않았을 때도 공간을 유지하여 텍스트가 흔들리지 않게 함 (CSS Border: White)
                        Spacer(modifier = Modifier.size(24.dp))
                    }
                }
            }

            // 기기 하단 여백 대응을 위해 약간의 패딩 추가
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ==========================================
// ── Previews ──
// ==========================================
@Preview(showBackground = true, backgroundColor = 0x80000000)
@Composable
private fun ArchiveReportBottomSheetPreview() {
    TalkQQuestTheme {
        ArchiveReportBottomSheet(
            isVisible = true,
            currentFilter = "성장 리포트",
            onFilterSelected = {},
            onDismissRequest = {}
        )
    }
}
