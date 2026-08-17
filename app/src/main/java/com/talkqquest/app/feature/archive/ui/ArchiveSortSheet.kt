package com.talkqquest.app.feature.archive.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp

import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.component.TqChoiceBottomSheet
import com.talkqquest.app.feature.archive.viewmodel.ArchiveSortType

@Composable
fun ArchiveSortSheet(
    isVisible: Boolean,
    currentSortType: ArchiveSortType,
    onSortSelected: (ArchiveSortType) -> Unit,
    onDismissRequest: () -> Unit
) {
    TqChoiceBottomSheet(
        visible = isVisible,
        onDismiss = onDismissRequest,
        cornerRadius = 36.dp,
        // CSS 패딩(상단 28px)을 정확히 맞추기 위해 드래그 핸들은 숨김 처리 (Report 필터 시트와 동일)
        dragHandle = false,
    ) {
        // CSS 명세: padding: 28px 16px 0px 20px; gap: 16px;
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 16.dp, top = 28.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ArchiveSortType.values().forEach { sortType ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        // CSS 명세는 height 24px 이지만, 터치 영역을 원활하게 하기 위해 상하 패딩 추가
                        .clickable {
                            onSortSelected(sortType)
                        }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sortType.title,
                        style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(),
                        color = Gray800
                    )
                    if (currentSortType == sortType) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "선택됨",
                            tint = Gray800,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        // 아이콘이 없더라도 공간을 차지하여 텍스트 정렬이 틀어지지 않게 함
                        Spacer(modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}
