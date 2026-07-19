package com.talkqquest.app.feature.archive.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

// 💡 캘린더 전용 신규 컬러 (Base/Gray 40)
private val Gray40 = Color(0xFFB5BEC6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TqCalendarBottomSheet(
    isVisible: Boolean,
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismissRequest: () -> Unit
) {
    if (isVisible) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val coroutineScope = rememberCoroutineScope()
        val haptic = LocalHapticFeedback.current

        var currentMonth by remember { mutableStateOf(YearMonth.from(initialDate)) }

        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp),
            containerColor = White,
            dragHandle = null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // [상단 커스텀 드래그 핸들]
                Box(modifier = Modifier.size(width = 36.dp, height = 4.dp).background(Gray600, CircleShape))

                // [캘린더 헤더 (월 변경)]
                Row(
                    modifier = Modifier.width(259.dp).height(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "이전 달", tint = Gray40,
                        modifier = Modifier.size(16.dp).clip(CircleShape).clickable { currentMonth = currentMonth.minusMonths(1) }
                    )
                    Text(
                        text = "${currentMonth.year}년 ${currentMonth.monthValue}월",
                        style = TqType.LabelL.copy(fontSize = 14.sp).figma(),
                        color = Gray600,
                        textAlign = TextAlign.Center
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "다음 달", tint = Gray40,
                        modifier = Modifier.size(16.dp).clip(CircleShape).clickable { currentMonth = currentMonth.plusMonths(1) }
                    )
                }

                // [요일 텍스트 영역]
                val weekDays = listOf("일", "월", "화", "수", "목", "금", "토")
                Row(
                    modifier = Modifier.width(259.dp).height(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    weekDays.forEach { day ->
                        Box(modifier = Modifier.size(width = 30.dp, height = 20.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = day,
                                style = TqType.BodyS.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium).figma(),
                                color = Gray40
                            )
                        }
                    }
                }

                // [날짜 그리드 영역]
                val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value % 7
                val daysInMonth = currentMonth.lengthOfMonth()

                Column(
                    modifier = Modifier.width(259.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (row in 0..5) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (col in 0..6) {
                                val dayIndex = row * 7 + col
                                val day = dayIndex - firstDayOfWeek + 1

                                if (day in 1..daysInMonth) {
                                    val thisDate = currentMonth.atDay(day)
                                    val isSelected = (thisDate == initialDate)

                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Primary600 else Color.Transparent)
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                onDateSelected(thisDate)
                                                coroutineScope.launch {
                                                    delay(250L)
                                                    sheetState.hide()
                                                    onDismissRequest()
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = day.toString(),
                                            style = TqType.BodyL.copy(fontWeight = FontWeight.SemiBold).figma(),
                                            color = if (isSelected) White else Gray600
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(30.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 💡 [프리뷰 영역 추가]
// ==========================================
@Preview(name = "캘린더 바텀 시트", showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun TqCalendarBottomSheetPreview() {
    TalkQQuestTheme {
        // 프리뷰에서는 isVisible을 true로 고정해두어야 화면에 표시됩니다.
        TqCalendarBottomSheet(
            isVisible = true,
            initialDate = LocalDate.of(2026, 7, 14), // 테스트용 날짜
            onDateSelected = {},
            onDismissRequest = {}
        )
    }
}